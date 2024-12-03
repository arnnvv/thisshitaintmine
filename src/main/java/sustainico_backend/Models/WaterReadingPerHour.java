package sustainico_backend.Models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@DynamoDBTable(tableName = "waterReadingPerHour")
public class WaterReadingPerHour {

    @DynamoDBHashKey(attributeName = "deviceId")
    private String deviceId;

    @DynamoDBAttribute(attributeName = "flowReading")
    private String flowReading;

    @DynamoDBAttribute(attributeName = "timestamp")
    private String timestamp;

    @DynamoDBRangeKey(attributeName = "fetchTimestamp")
    private String fetchTimestamp;

    @DynamoDBAttribute(attributeName = "readingId")
    private String readingId;

    public WaterReadingPerHour() {}

    public WaterReadingPerHour(String deviceId, String flowReading, String timestamp, String fetchTimestamp, String readingId) {
        this.deviceId = deviceId;
        this.flowReading = flowReading;
        this.timestamp = timestamp;
        this.fetchTimestamp = fetchTimestamp;
        this.readingId = readingId;
    }

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

    public String getFetchTimestamp() {
        return fetchTimestamp;
    }

    public void setFetchTimestamp(String fetchTimestamp) {
        this.fetchTimestamp = fetchTimestamp;
    }

    public String getReadingId() {
        return readingId;
    }

    public void setReadingId(String readingId) {
        this.readingId = readingId;
    }

    public void generateReadingId() {
        this.readingId = this.deviceId + "-" + this.fetchTimestamp;
    }
}
