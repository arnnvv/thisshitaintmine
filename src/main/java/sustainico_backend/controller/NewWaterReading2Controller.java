package sustainico_backend.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sustainico_backend.Models.NewWaterReading2;
import sustainico_backend.service.NewWaterReading2Service;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/newWaterReading2")
public class NewWaterReading2Controller {
    @Autowired
    private NewWaterReading2Service service;

    @PostMapping("/send")
    public ResponseEntity<?> createReading(@RequestBody Map<String, Object> newReading2) {
        NewWaterReading2 newWaterReading2 = mapToNewWaterReading(newReading2);
        long currentEpochTimestamp = Instant.now().getEpochSecond();
        newWaterReading2.setTimestamp(String.valueOf(currentEpochTimestamp));
        newWaterReading2.generateReadingId();
        NewWaterReading2 savedReading = service.saveReading(newWaterReading2);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private NewWaterReading2 mapToNewWaterReading(Map<String, Object> newReading2) {
        NewWaterReading2 newWaterReading2 = new NewWaterReading2();
        newWaterReading2.setDeviceId(String.valueOf(newReading2.get("DeviceID")));
        newWaterReading2.setStatus((Map<String, Boolean>) newReading2.get("Status"));
        newWaterReading2.setLiters(((Number) newReading2.get("Liters")).longValue());
        newWaterReading2.setMilliliters(((Number) newReading2.get("Milliliters")).longValue());
        return newWaterReading2;
    }

    @PostMapping("/latest")
    public ResponseEntity<?> getLatestReading(@RequestBody Map<String, String> request) {
        try {
            String deviceId = request.get("deviceId");
            String timeFilter = request.get("timeFilter"); // "day", "month", or "year"
            
            if (deviceId == null || deviceId.trim().isEmpty()) {
                return new ResponseEntity<>("Device ID is required", HttpStatus.BAD_REQUEST);
            }
            
            if (timeFilter == null || timeFilter.trim().isEmpty()) {
                return new ResponseEntity<>("Time filter is required", HttpStatus.BAD_REQUEST);
            }

            Map<String, Object> consumptionData = service.getWaterConsumptionData(deviceId, timeFilter);
            
            if (consumptionData == null || consumptionData.isEmpty()) {
                return new ResponseEntity<>("No readings found for device ID: " + deviceId, 
                    HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(consumptionData, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving consumption data: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
