package sustainico_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sustainico_backend.Models.NewWaterReading2;
import sustainico_backend.rep.NewWaterReading2Repository;
import java.time.*;
import java.util.*;

@Service
public class NewWaterReading2Service {
    @Autowired
    private NewWaterReading2Repository repository;

    public NewWaterReading2 saveReading(NewWaterReading2 newWaterReading) {
        newWaterReading.generateReadingId();
        return repository.save(newWaterReading);
    }

    public Map<String, Object> getWaterConsumptionData(String deviceId, String timeFilter, String targetDate) {
        // Parse the target date
        LocalDateTime targetDateTime = LocalDate.parse(targetDate).atStartOfDay();
        
        // Calculate start and end timestamps based on the filter and target date
        long startTime, endTime, lookbackStartTime;
        List<String> labels;
        
        switch (timeFilter) {
            case "day":
                startTime = targetDateTime.toEpochSecond(ZoneOffset.UTC);
                endTime = targetDateTime.plusDays(1).toEpochSecond(ZoneOffset.UTC);
                lookbackStartTime = targetDateTime.minusDays(1).toEpochSecond(ZoneOffset.UTC);
                labels = generate24HourLabels();
                break;
                
            case "month":
                LocalDateTime startOfMonth = targetDateTime.withDayOfMonth(1);
                startTime = startOfMonth.toEpochSecond(ZoneOffset.UTC);
                endTime = startOfMonth.plusMonths(1).toEpochSecond(ZoneOffset.UTC);
                lookbackStartTime = startOfMonth.minusMonths(1).toEpochSecond(ZoneOffset.UTC);
                labels = generateMonthLabels(startOfMonth.getYear(), startOfMonth.getMonthValue());
                break;
                
            case "year":
                LocalDateTime startOfYear = targetDateTime.withDayOfYear(1);
                startTime = startOfYear.toEpochSecond(ZoneOffset.UTC);
                endTime = startOfYear.plusYears(1).toEpochSecond(ZoneOffset.UTC);
                lookbackStartTime = startOfYear.minusYears(1).toEpochSecond(ZoneOffset.UTC);
                labels = generateYearLabels();
                break;
                
            default:
                throw new IllegalArgumentException("Invalid time filter");
        }

        // Get the last non-zero reading before the start time
        List<NewWaterReading2> previousReadings = repository.findReadingsBetweenTimestamps(
            deviceId, 
            String.valueOf(lookbackStartTime), 
            String.valueOf(startTime)
        );

        double baselineConsumption = getLastNonZeroReading(previousReadings);

        // Get readings for the requested period
        List<NewWaterReading2> readings = repository.findReadingsBetweenTimestamps(
            deviceId, 
            String.valueOf(startTime), 
            String.valueOf(endTime)
        );

        List<Double> consumptionData = calculateConsumption(readings, timeFilter, labels.size(), startTime, baselineConsumption);

        Map<String, Object> response = new HashMap<>();
        response.put("labels", labels);
        response.put("data", consumptionData);
        response.put("startTime", startTime);
        response.put("endTime", endTime);
        return response;
    }

    private double getLastNonZeroReading(List<NewWaterReading2> readings) {
        if (readings == null || readings.isEmpty()) {
            return 0.0;
        }

        // Sort readings by timestamp in descending order
        readings.sort((a, b) -> Long.compare(
            Long.parseLong(b.getTimestamp()),
            Long.parseLong(a.getTimestamp())
        ));

        // Find the last non-zero reading
        for (NewWaterReading2 reading : readings) {
            double total = reading.getLiters() + (reading.getMilliliters() / 1000.0);
            if (total > 0) {
                return total;
            }
        }

        return 0.0;
    }

    private List<Double> calculateConsumption(
        List<NewWaterReading2> readings, 
        String timeFilter, 
        int intervals, 
        long startTime,
        double baselineConsumption
    ) {
        List<Double> consumption = new ArrayList<>();
        for (int i = 0; i < intervals; i++) {
            consumption.add(0.0);
        }

        if (readings == null || readings.isEmpty()) {
            return consumption;
        }

        // Sort readings by timestamp
        readings.sort((a, b) -> Long.compare(
            Long.parseLong(a.getTimestamp()),
            Long.parseLong(b.getTimestamp())
        ));

        // Group readings by interval
        Map<Integer, List<NewWaterReading2>> readingsByInterval = new HashMap<>();
        for (NewWaterReading2 reading : readings) {
            int interval = getIntervalIndex(Long.parseLong(reading.getTimestamp()), timeFilter);
            if (interval >= 0 && interval < intervals) {
                readingsByInterval.computeIfAbsent(interval, k -> new ArrayList<>()).add(reading);
            }
        }

        // Process each interval
        for (int interval = 0; interval < intervals; interval++) {
            double intervalConsumption = calculateIntervalConsumption(
                readingsByInterval.get(interval),
                interval,
                readingsByInterval,
                timeFilter,
                intervals,
                baselineConsumption
            );
            consumption.set(interval, intervalConsumption);
        }

        return consumption;
    }

    private double calculateIntervalConsumption(
        List<NewWaterReading2> intervalReadings,
        int currentInterval,
        Map<Integer, List<NewWaterReading2>> readingsByInterval,
        String timeFilter,
        int totalIntervals,
        double baselineConsumption
    ) {
        if (intervalReadings == null || intervalReadings.isEmpty()) {
            return 0.0;
        }

        // Sort interval readings by timestamp
        intervalReadings.sort((a, b) -> Long.compare(
            Long.parseLong(a.getTimestamp()),
            Long.parseLong(b.getTimestamp())
        ));

        // Get first and last valid readings in the interval
        NewWaterReading2 firstReading = findFirstValidReading(intervalReadings);
        NewWaterReading2 lastReading = findLastValidReading(intervalReadings);

        if (firstReading == null || lastReading == null) {
            return 0.0;
        }

        // Calculate consumption
        double firstTotal = firstReading.getLiters() + (firstReading.getMilliliters() / 1000.0);
        double lastTotal = lastReading.getLiters() + (lastReading.getMilliliters() / 1000.0);

        // If this is the first interval and the first reading is 0, use the baseline
        if (currentInterval == 0 && firstTotal == 0) {
            firstTotal = baselineConsumption;
        }

        return Math.max(0, lastTotal - firstTotal);
    }

    // ... [rest of the helper methods remain unchanged] ...
    private NewWaterReading2 findFirstValidReading(List<NewWaterReading2> readings) {
        if (readings == null) return null;
        return readings.stream()
            .findFirst()
            .orElse(null);
    }

    private NewWaterReading2 findLastValidReading(List<NewWaterReading2> readings) {
        if (readings == null) return null;
        return readings.stream()
            .reduce((first, second) -> second)
            .orElse(null);
    }

    private int getIntervalIndex(long timestamp, String timeFilter) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(timestamp), 
            ZoneId.systemDefault()
        );
        
        switch (timeFilter) {
            case "day":
                return dateTime.getHour();
            case "month":
                return dateTime.getDayOfMonth() - 1;
            case "year":
                return dateTime.getMonthValue() - 1;
            default:
                return -1;
        }
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
