package sustainico_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sustainico_backend.Models.WaterReadingPerHour;
import sustainico_backend.service.WaterReadingPerHourService;

import java.util.List;

@RestController
@RequestMapping("/readingperhour")
public class WaterReadingPerHourController {

    @Autowired
    private WaterReadingPerHourService waterReadingPerHourService;

    @GetMapping
    public List<WaterReadingPerHour> getAllReadings() {
        return waterReadingPerHourService.findAll();
    }

//    @GetMapping("/{deviceId}")
//    public List<WaterReadingPerHour> getReadingsByDeviceId(@PathVariable String deviceId) {
//        return waterReadingPerHourService.getWaterReadingsByDeviceId(deviceId);
//    }
}
