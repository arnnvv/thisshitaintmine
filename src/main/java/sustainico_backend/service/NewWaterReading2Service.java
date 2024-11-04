package sustainico_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sustainico_backend.Models.NewWaterReading2;
import sustainico_backend.rep.NewWaterReading2Repository;
import java.time.*;
import java.util.*;

@Service
public class NewWaterReading2Service {
    private static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");
    
    @Autowired
    private NewWaterReading2Repository repository;

    public NewWaterReading2 saveReading(NewWaterReading2 newWaterReading) {
        newWaterReading.generateReadingId();
        return repository.save(newWaterReading);
    }

    public Map<String, Object> getWaterConsumptionData(String deviceId, String timeFilter, String targetDate) {
        // Parse the target date in Indian timezone
        LocalDateTime targetDateTime = LocalDate.parse(targetDate)
            .atStartOfDay(INDIA_ZONE)
            .toLocalDateTime();
        
        // Calculate start and end timestamps based on the filter and target date
        long startTime, endTime;
        List<String> labels;
        
        switch (timeFilter) {
            case "day":
                // Convert local midnight to epoch seconds
                startTime = targetDateTime.atZone(INDIA_ZONE).toEpochSecond();
                endTime = targetDateTime.plusDays(1).atZone(INDIA_ZONE).toEpochSecond();
                labels = generate24HourLabels();
                break;
                
            case "month":
                LocalDateTime startOfMonth = targetDateTime.withDayOfMonth(1);
                startTime = startOfMonth.atZone(INDIA_ZONE).toEpochSecond();
                endTime = startOfMonth.plusMonths(1).atZone(INDIA_ZONE).toEpochSecond();
                labels = generateMonthLabels(startOfMonth.getYear(), startOfMonth.getMonthValue());
                break;
                
            case "year":
                LocalDateTime startOfYear = targetDateTime.withDayOfYear(1);
                startTime = startOfYear.atZone(INDIA_ZONE).toEpochSecond();
                endTime = startOfYear.plusYears(1).atZone(INDIA_ZONE).toEpochSecond();
                labels = generateYearLabels();
                break;
                
            default:
                throw new IllegalArgumentException("Invalid time filter");
        }

        // Get readings including previous period for proper calculations
        long extendedStartTime = switch (timeFilter) {
            case "day" -> startTime - 3600; // One hour before
            case "month" -> startTime - 86400; // One day before
            case "year" -> startTime - 86400 * 31L; // One month before (approximate)
            default -> startTime;
        };

        List<NewWaterReading2> readings = repository.findReadingsBetweenTimestamps(
            deviceId, 
            String.valueOf(extendedStartTime), 
            String.valueOf(endTime)
        );

        List<Double> consumptionData = calculateConsumption(readings, timeFilter, labels.size(), startTime, endTime);

        Map<String, Object> response = new HashMap<>();
        response.put("labels", labels);
        response.put("data", consumptionData);
        response.put("startTime", startTime);
        response.put("endTime", endTime);
        return response;
    }

    private List<Double> calculateConsumption(
        List<NewWaterReading2> readings, 
        String timeFilter, 
        int intervals,
        long startTime,
        long endTime
    ) {
        if (readings == null || readings.isEmpty()) {
            return new ArrayList<>(Collections.nCopies(intervals, 0.0));
        }

        readings.sort((a, b) -> Long.compare(
            Long.parseLong(a.getTimestamp()),
            Long.parseLong(b.getTimestamp())
        ));

        return switch (timeFilter) {
            case "day" -> calculateHourlyConsumption(readings, startTime, endTime);
            case "month" -> calculateDailyConsumption(readings, startTime, endTime);
            case "year" -> calculateMonthlyConsumption(readings, startTime, endTime);
            default -> new ArrayList<>(Collections.nCopies(intervals, 0.0));
        };
    }

    private List<Double> calculateHourlyConsumption(
        List<NewWaterReading2> readings,
        long startTime,
        long endTime
    ) {
        List<Double> hourlyConsumption = new ArrayList<>(Collections.nCopies(24, 0.0));
        
        // Create hour boundaries in IST
        List<Long> hourBoundaries = new ArrayList<>();
        LocalDateTime startDateTime = Instant.ofEpochSecond(startTime)
            .atZone(INDIA_ZONE)
            .toLocalDateTime();
            
        for (int i = 0; i <= 24; i++) {
            hourBoundaries.add(
                startDateTime.plusHours(i)
                    .atZone(INDIA_ZONE)
                    .toEpochSecond()
            );
        }

        // Find the last non-zero reading before each hour boundary
        Map<Integer, Double> lastNonZeroReadings = new HashMap<>();
        double previousNonZeroValue = 0.0;
        
        for (int hour = 0; hour <= 24; hour++) {
            long hourBoundary = hourBoundaries.get(hour);
            double lastNonZeroBeforeBoundary = previousNonZeroValue;
            
            // Find the last non-zero reading before this hour boundary
            for (int i = readings.size() - 1; i >= 0; i--) {
                NewWaterReading2 reading = readings.get(i);
                long readingTime = Long.parseLong(reading.getTimestamp());
                
                if (readingTime < hourBoundary) {
                    double totalReading = reading.getLiters() + (reading.getMilliliters() / 1000.0);
                    if (totalReading > 0) {
                        lastNonZeroBeforeBoundary = totalReading;
                        break;
                    }
                }
            }
            
            lastNonZeroReadings.put(hour, lastNonZeroBeforeBoundary);
            previousNonZeroValue = lastNonZeroBeforeBoundary;
        }

        // Calculate consumption for each hour
        for (int hour = 0; hour < 24; hour++) {
            double endValue = lastNonZeroReadings.get(hour + 1);
            double startValue = lastNonZeroReadings.get(hour);
            hourlyConsumption.set(hour, Math.max(0, endValue - startValue));
        }

        return hourlyConsumption;
    }

    private List<Double> calculateDailyConsumption(
        List<NewWaterReading2> readings,
        long startTime,
        long endTime
    ) {
        LocalDateTime startDateTime = Instant.ofEpochSecond(startTime)
            .atZone(INDIA_ZONE)
            .toLocalDateTime();
            
        YearMonth yearMonth = YearMonth.from(startDateTime);
        int daysInMonth = yearMonth.lengthOfMonth();
        List<Double> dailyConsumption = new ArrayList<>(Collections.nCopies(daysInMonth, 0.0));
        
        // Create day boundaries in IST
        List<Long> dayBoundaries = new ArrayList<>();
        for (int i = 0; i <= daysInMonth; i++) {
            dayBoundaries.add(
                startDateTime.plusDays(i)
                    .atZone(INDIA_ZONE)
                    .toEpochSecond()
            );
        }

        Map<Integer, Double> lastNonZeroReadings = new HashMap<>();
        double previousNonZeroValue = 0.0;
        
        for (int day = 0; day <= daysInMonth; day++) {
            long dayBoundary = dayBoundaries.get(day);
            double lastNonZeroBeforeBoundary = previousNonZeroValue;
            
            for (int i = readings.size() - 1; i >= 0; i--) {
                NewWaterReading2 reading = readings.get(i);
                long readingTime = Long.parseLong(reading.getTimestamp());
                
                if (readingTime < dayBoundary) {
                    double totalReading = reading.getLiters() + (reading.getMilliliters() / 1000.0);
                    if (totalReading > 0) {
                        lastNonZeroBeforeBoundary = totalReading;
                        break;
                    }
                }
            }
            
            lastNonZeroReadings.put(day, lastNonZeroBeforeBoundary);
            previousNonZeroValue = lastNonZeroBeforeBoundary;
        }

        for (int day = 0; day < daysInMonth; day++) {
            double endValue = lastNonZeroReadings.get(day + 1);
            double startValue = lastNonZeroReadings.get(day);
            dailyConsumption.set(day, Math.max(0, endValue - startValue));
        }

        return dailyConsumption;
    }

    private List<Double> calculateMonthlyConsumption(
        List<NewWaterReading2> readings,
        long startTime,
        long endTime
    ) {
        List<Double> monthlyConsumption = new ArrayList<>(Collections.nCopies(12, 0.0));
        
        // Convert start time to IST for month calculations
        LocalDateTime startDateTime = Instant.ofEpochSecond(startTime)
            .atZone(INDIA_ZONE)
            .toLocalDateTime();
        
        // Create month boundaries in IST
        List<Long> monthBoundaries = new ArrayList<>();
        LocalDateTime current = startDateTime;
        for (int i = 0; i <= 12; i++) {
            monthBoundaries.add(
                current.atZone(INDIA_ZONE).toEpochSecond()
            );
            current = current.plusMonths(1);
        }

        Map<Integer, Double> lastNonZeroReadings = new HashMap<>();
        double previousNonZeroValue = 0.0;
        
        for (int month = 0; month <= 12; month++) {
            long monthBoundary = monthBoundaries.get(month);
            double lastNonZeroBeforeBoundary = previousNonZeroValue;
            
            for (int i = readings.size() - 1; i >= 0; i--) {
                NewWaterReading2 reading = readings.get(i);
                long readingTime = Long.parseLong(reading.getTimestamp());
                
                if (readingTime < monthBoundary) {
                    double totalReading = reading.getLiters() + (reading.getMilliliters() / 1000.0);
                    if (totalReading > 0) {
                        lastNonZeroBeforeBoundary = totalReading;
                        break;
                    }
                }
            }
            
            lastNonZeroReadings.put(month, lastNonZeroBeforeBoundary);
            previousNonZeroValue = lastNonZeroBeforeBoundary;
        }

        for (int month = 0; month < 12; month++) {
            double endValue = lastNonZeroReadings.get(month + 1);
            double startValue = lastNonZeroReadings.get(month);
            monthlyConsumption.set(month, Math.max(0, endValue - startValue));
        }

        return monthlyConsumption;
    }

    private List<String> generate24HourLabels() {
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            labels.add(String.format("%02d:00", i));
        }
        return labels;
    }

    private List<String> generateMonthLabels(int year, int month) {
        List<String> labels = new ArrayList<>();
        YearMonth yearMonth = YearMonth.of(year, month);
        for (int i = 1; i <= yearMonth.lengthOfMonth(); i++) {
            labels.add(String.valueOf(i));
        }
        return labels;
    }

    private List<String> generateYearLabels() {
        return new ArrayList<>(Arrays.asList(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        ));
    }
}
