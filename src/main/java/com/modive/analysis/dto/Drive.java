package com.modive.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
public class Drive {
    // Partition Key
    private String userId;
    // Sort Key
    private String driveId;

    private Instant startTime;
    private Instant endTime;
    private int activeDriveDurationSec;

    private List<Instant> suddenAccelerations;
    private List<Instant> sharpTurns;
    private List<SpeedLog> speedLogs;
    private List<StartEndTime> idlingPeriods;
    private List<SpeedRate> speedRate;
    private List<StartEndTime> reactionTimes;
    private List<Instant> laneDepartures;
    private List<StartEndTime> followingDistanceEvents;
    private List<Instant> inactiveMoments;


    //<editor-fold desc="# Inner Classes">
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SpeedLog {
        private int period;
        private int maxSpeed;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StartEndTime {
        private Instant startTime;
        private Instant endTime;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SpeedRate {
        private String tag;
        private int ratio;
    }
    //</editor-fold>
}