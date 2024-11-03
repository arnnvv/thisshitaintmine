package sustainico_backend.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sustainico_backend.Models.NewWaterReading2;
import sustainico_backend.rep.NewWaterReading2Repository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class NewWaterReading2Service {
    @Autowired
    private NewWaterReading2Repository repository;

    public NewWaterReading2 saveReading(NewWaterReading2 newWaterReading) {
        newWaterReading.generateReadingId();
        return repository.save(newWaterReading);
    }

    public Map<String, Object> getWaterConsumptionData(String deviceId, String timeFilter) {
        long currentTime = Instant.now().getEpochSecond();
        long startTime;
        List<String> labels;
        
        // Calculate start time based on filter
        switch (timeFilter) {
            case "day":
                startTime = currentTime - 24 * 60 * 60; // 24 hours ago
                labels = generate24HourLabels();
                break;
            case "month":
                startTime = currentTime - 30L * 24 * 60 * 60; // 30 days ago
                labels = generateMonthLabels();
                break;
            case "year":
                startTime = currentTime - 365L * 24 * 60 * 60; // 365 days ago
                labels = generateYearLabels();
                break;
            default:
                throw new IllegalArgumentException("Invalid time filter");
        }

        List<NewWaterReading2> readings = repository.findReadingsBetweenTimestamps(
            deviceId, 
            String.valueOf(startTime), 
            String.valueOf(currentTime)
        );

        List<Double> consumptionData = calculateConsumption(readings, timeFilter, labels.size());

        Map<String, Object> response = new HashMap<>();
        response.put("labels", labels);
        response.put("data", consumptionData);
        return response;
    }

    private List<String> generate24HourLabels() {
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            labels.add(String.format("%02d:00", i));
        }
        return labels;
    }

    private List<String> generateMonthLabels() {
        List<String> labels = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 1; i <= now.toLocalDate().lengthOfMonth(); i++) {
            labels.add(String.valueOf(i));
        }
        return labels;
    }

    private List<String> generateYearLabels() {
        return Arrays.asList(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        );
    }

    private List<Double> calculateConsumption(List<NewWaterReading2> readings, String timeFilter, int intervals) {
        if (readings.isEmpty()) {
            return Collections.nCopies(intervals, 0.0);
        }

        List<Double> consumption = new ArrayList<>(Collections.nCopies(intervals, 0.0));
        
        // Sort readings by timestamp
        readings.sort(Comparator.comparing(NewWaterReading2::getTimestamp));
        
        for (int i = 1; i < readings.size(); i++) {
            NewWaterReading2 current = readings.get(i);
            NewWaterReading2 previous = readings.get(i - 1);
            
            long currentTimestamp = Long.parseLong(current.getTimestamp());
            int interval = getIntervalIndex(currentTimestamp, timeFilter);
            
            if (interval >= 0 && interval < intervals) {
                double litersDiff = current.getLiters() - previous.getLiters();
                double mlDiff = (current.getMilliliters() - previous.getMilliliters()) / 1000.0;
                consumption.set(interval, consumption.get(interval) + litersDiff + mlDiff);
            }
        }
        
        return consumption;
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
}
