package com.modive.analysis.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@DynamoDBTable(tableName = "drive")
@Data
public class Drive {
    // Partition Key
    private String userId;
    // Sort Key
    private String driveId;


    @DynamoDBTypeConverted(converter = TypeConverter.InstantConverter.class)
    private Instant startTime;

    @DynamoDBTypeConverted(converter = TypeConverter.InstantConverter.class)
    private Instant endTime;

    private int activeDriveDurationSec;

    @DynamoDBTypeConverted(converter = TypeConverter.InstantListConverter.class)
    private List<Instant> suddenAccelerations;

    @DynamoDBTypeConverted(converter = TypeConverter.InstantListConverter.class)
    private List<Instant> sharpTurns;

    private List<SpeedLog> speedLogs;

    private List<StartEndTime> idlingPeriods;

    private List<SpeedRate> speedRate;

    private List<StartEndTime> reactionTimes;

    @DynamoDBTypeConverted(converter = TypeConverter.InstantListConverter.class)
    private List<Instant> laneDepartures;

    private List<StartEndTime> followingDistanceEvents;

    @DynamoDBTypeConverted(converter = TypeConverter.InstantListConverter.class)
    private List<Instant> inactiveMoments;

    // <editor-fold desc="# Getter for key">
    @DynamoDBHashKey(attributeName = "userId")
    public String getUserId() {
        return userId;
    }

    @DynamoDBRangeKey(attributeName = "driveId")
    public String getDriveId() {
        return driveId;
    }
    // </editor-fold>

    //<editor-fold desc="# Inner Classes">
    @DynamoDBDocument
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SpeedLog {
        private int period;
        private int maxSpeed;
    }

    @DynamoDBDocument
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StartEndTime {
        @DynamoDBTypeConverted(converter = TypeConverter.InstantConverter.class)
        private Instant startTime;

        @DynamoDBTypeConverted(converter = TypeConverter.InstantConverter.class)
        private Instant endTime;
    }

    @DynamoDBDocument
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SpeedRate {
        private String tag;
        private int ratio;
    }

    @DynamoDBDocument
    @Data
    public static class TimeWithFlag {
        @DynamoDBTypeConverted(converter = TypeConverter.InstantConverter.class)
        private Instant time;
        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.BOOL)
        private boolean flag;
    }
    //</editor-fold>
}

class TypeConverter {

    //<editor-folder desc="# DynamoDB instant">
    // Instant 변환기
    public static class InstantConverter implements DynamoDBTypeConverter<String, Instant> {
        @Override
        public String convert(Instant object) {
            return object.toString();
        }

        @Override
        public Instant unconvert(String object) {
            return Instant.parse(object);
        }
    }

    // List<Instant> 변환기
    public static class InstantListConverter implements DynamoDBTypeConverter<List<String>, List<Instant>> {
        @Override
        public List<String> convert(List<Instant> instants) {
            return instants.stream().map(Instant::toString).collect(Collectors.toList());
        }

        @Override
        public List<Instant> unconvert(List<String> strings) {
            return strings.stream().map(Instant::parse).collect(Collectors.toList());
        }
    }
    //</editor-folder>
}