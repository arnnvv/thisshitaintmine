package sustainico_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sustainico_backend.Models.NewWaterReading2;
import sustainico_backend.rep.NewWaterReading2Repository;

@Service
public class NewWaterReading2Service {
    @Autowired
    private NewWaterReading2Repository repository;

    // Existing save method
    public NewWaterReading2 saveReading(NewWaterReading2 newWaterReading) {
        newWaterReading.generateReadingId();  // Automatically generate the reading ID
        return repository.save(newWaterReading);
    }

    // New method for getting latest reading
    public NewWaterReading2 getLatestReadingByDeviceId(String deviceId) {
        return repository.findLatestByDeviceId(deviceId);
    }
}
