package com.modive.analysis.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DynamoDBTableInitializer {
    private final AmazonDynamoDB dynamoDB;

    public DynamoDBTableInitializer(@Value("${aws.region}") String region) {
        this.dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(region)
                .build();
    }

    @PostConstruct
    public void createDriveIfNotExists() {
        ListTablesResult tables = dynamoDB.listTables();
        if (!tables.getTableNames().contains("drive")) {
            CreateTableRequest request = new CreateTableRequest()
                    .withTableName("drive")
                    .withKeySchema(
                        new KeySchemaElement("userId", KeyType.HASH),
                        new KeySchemaElement("driveId", KeyType.RANGE)
                    )
                    .withAttributeDefinitions(
                        new AttributeDefinition("userId", ScalarAttributeType.S),
                        new AttributeDefinition("driveId", ScalarAttributeType.S)
                    )
                    .withBillingMode(BillingMode.PAY_PER_REQUEST);

            dynamoDB.createTable(request);
            System.out.println("✅ DynamoDB 테이블 'drive' 생성됨");
        } else {
            System.out.println("ℹ️ DynamoDB 테이블 'drive' 이미 존재함");
        }
    }

}