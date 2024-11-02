package sustainico_backend.Models;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "subscriber")
public class Subscriber {

    @DynamoDBAutoGeneratedKey
    @DynamoDBHashKey(attributeName = "subscriberId")
    private String subscriberId;

    @DynamoDBAttribute(attributeName = "name")
    private String name;

    @DynamoDBAttribute(attributeName = "contact")
    private String contact;

    @DynamoDBAttribute(attributeName = "email")
    private String email;

    @DynamoDBAttribute(attributeName = "timestamp")
    private String timestamp = String.valueOf(Instant.now().getEpochSecond());

}