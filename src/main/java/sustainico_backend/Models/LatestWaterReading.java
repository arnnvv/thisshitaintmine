package sustainico_backend.Models;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@DynamoDBTable(tableName = "latestWaterReading")
public class LatestWaterReading {

    @DynamoDBHashKey(attributeName = "deviceId")
    private String deviceId;

    @DynamoDBAttribute(attributeName = "latestFlowReading")
    private String latestFlowReading;

    @DynamoDBAttribute(attributeName = "firstFlowReading")
    private String firstFlowReading;

    @DynamoDBAttribute(attributeName = "firstReadingOfMonth")
    private String firstReadingOfMonth;

    @DynamoDBAttribute(attributeName = "lastReadingOfMonth")
    private String lastReadingOfMonth;

    @DynamoDBAttribute(attributeName = "timestamp")
    private String timestamp;

    @DynamoDBAttribute(attributeName = "timestampOfMonth")
    private String timestampOfMonth;

    // Default Constructor
    public LatestWaterReading() {}

    // All-Args Constructor
    public LatestWaterReading(String deviceId, String latestFlowReading, String firstFlowReading,
                              String firstReadingOfMonth, String lastReadingOfMonth,
                              String timestamp, String timestampOfMonth) {
        this.deviceId = deviceId;
        this.latestFlowReading = latestFlowReading;
        this.firstFlowReading = firstFlowReading;
        this.firstReadingOfMonth = firstReadingOfMonth;
        this.lastReadingOfMonth = lastReadingOfMonth;
        this.timestamp = timestamp;
        this.timestampOfMonth = timestampOfMonth;
    }

    // Getters and Setters
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getLatestFlowReading() {
        return latestFlowReading;
    }

    public void setLatestFlowReading(String latestFlowReading) {
        this.latestFlowReading = latestFlowReading;
    }

    public String getFirstFlowReading() {
        return firstFlowReading;
    }

    public void setFirstFlowReading(String firstFlowReading) {
        this.firstFlowReading = firstFlowReading;
    }

    public String getFirstReadingOfMonth() {
        return firstReadingOfMonth;
    }

    public void setFirstReadingOfMonth(String firstReadingOfMonth) {
        this.firstReadingOfMonth = firstReadingOfMonth;
    }

    public String getLastReadingOfMonth() {
        return lastReadingOfMonth;
    }

    public void setLastReadingOfMonth(String lastReadingOfMonth) {
        this.lastReadingOfMonth = lastReadingOfMonth;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestampOfMonth() {
        return timestampOfMonth;
    }

    public void setTimestampOfMonth(String timestampOfMonth) {
        this.timestampOfMonth = timestampOfMonth;
    }
}
