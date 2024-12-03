package sustainico_backend.Models;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@DynamoDBTable(tableName = "waterReading")
public class WaterReading {

    @DynamoDBHashKey(attributeName = "deviceId")
    private String deviceId;

    @DynamoDBAttribute(attributeName = "flowReading")
    private String flowReading;

    @DynamoDBRangeKey(attributeName = "timestamp")
    private String timestamp;

    @DynamoDBAttribute(attributeName = "readingId")
    private String readingId;

    // Default Constructor
    public WaterReading() {}

    // All-Args Constructor
    public WaterReading(String deviceId, String flowReading, String timestamp, String readingId) {
        this.deviceId = deviceId;
        this.flowReading = flowReading;
        this.timestamp = timestamp;
        this.readingId = readingId;
    }

    // Getters and Setters
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getFlowReading() {
        return flowReading;
    }

    public void setFlowReading(String flowReading) {
        this.flowReading = flowReading;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getReadingId() {
        return readingId;
    }

    public void setReadingId(String readingId) {
        this.readingId = readingId;
    }

    // Method to generate readingId
    public void generateReadingId() {
        this.readingId = this.deviceId + "-" + this.timestamp;
    }
}
