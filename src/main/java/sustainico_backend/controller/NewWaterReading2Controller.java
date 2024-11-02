package sustainico_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sustainico_backend.Models.NewWaterReading;
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
        // Get the current epoch timestamp
        long currentEpochTimestamp = Instant.now().getEpochSecond();
        newWaterReading2.setTimestamp(String.valueOf(currentEpochTimestamp));
        // Generate readingId
        newWaterReading2.generateReadingId();


        NewWaterReading2 savedReading = service.saveReading(newWaterReading2);
        return new ResponseEntity<>( HttpStatus.OK);
    }

    private NewWaterReading2 mapToNewWaterReading(Map<String, Object> newReading2) {
        NewWaterReading2 newWaterReading2 = new NewWaterReading2();
        // Convert DeviceID to String before mapping
        newWaterReading2.setDeviceId(String.valueOf(newReading2.get("DeviceID")));

        // Map Status
        newWaterReading2.setStatus((Map<String, Boolean>) newReading2.get("Status"));

        // Convert Liters and Milliliters from Integer to Long safely
        newWaterReading2.setLiters(((Number) newReading2.get("Liters")).longValue());
        newWaterReading2.setMilliliters(((Number) newReading2.get("Milliliters")).longValue());

        return newWaterReading2;
    }

    @PostMapping("/latest")
    public ResponseEntity<?> getLatestReading(@RequestBody Map<String, String> request) {
      System.out.println("IN LATEST");
        try {
          System.out.println("IN TRY");
            String deviceId = request.get("deviceId");
            
            if (deviceId == null || deviceId.trim().isEmpty()) {
                return new ResponseEntity<>("Device ID is required", HttpStatus.BAD_REQUEST);
            }

            NewWaterReading2 latestReading = service.getLatestReadingByDeviceId(deviceId);
            
            if (latestReading == null) {
                return new ResponseEntity<>("No readings found for device ID: " + deviceId, 
                    HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(latestReading, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving latest reading: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
