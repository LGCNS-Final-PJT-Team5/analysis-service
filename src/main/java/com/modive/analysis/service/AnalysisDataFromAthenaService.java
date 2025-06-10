package com.modive.analysis.service;

import com.modive.analysis.entity.Drive;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AnalysisDataFromAthenaService {

    public Drive analysisData(List<Map<String, String>> data) {

        Drive drive = new Drive();

        int activeDriveDurationSec = 0;
        List<Drive.SpeedLog> speedLogs = new ArrayList<>();
        List<Drive.StartEndTime> idlingPeriods = new ArrayList<>();
        List<Drive.SpeedRate> speedRate = new ArrayList<>();

//        List<Drive.StartEndTime> reactionTimes;
//        List<Drive.StartEndTime> followingDistanceEvents;

        drive.setDriveId(data.get(0).get("driveid"));
        drive.setUserId(data.get(0).get("userid"));
        drive.setStartTime(stringToInstant(data.get(0).get("time")));
        drive.setEndTime(stringToInstant(data.get(data.size() - 1).get("time")));

        Instant idlingStartTime = null;
        double currentMaxSpeed = 0;
        Instant periodStartTime = null;
        int period = 1;
        int low = 0;
        int middle = 0;
        int high = 0;

        Duration periodDuration = Duration.ofSeconds(20); // period 주기

        for (int i = 0; i < data.size(); i++) {

            // 현재 데이터
            Map<String, String> current = data.get(i);
            Instant now = stringToInstant(current.get("time"));
            Double speed = Double.parseDouble(current.get("velocity"));

            // 공회전 분석
            if (speed < 1) { if (idlingStartTime == null) { idlingStartTime = now; } }
            else {
                if (idlingStartTime != null) {
                    idlingPeriods.add(new Drive.StartEndTime(idlingStartTime, now));
                    idlingStartTime = null;
                }
            }

            // 최고속도 분석
            if (periodStartTime == null) { periodStartTime = now; }
            else {
                if (Duration.between(periodStartTime, now).compareTo(periodDuration) < 0 ) {
                    if (speed > currentMaxSpeed ) {currentMaxSpeed = speed;}
                }
                else {
                    speedLogs.add(new Drive.SpeedLog(period, (int) currentMaxSpeed));
                    period++;
                    periodStartTime = now;
                    currentMaxSpeed = speed;
                }
            }

            // 정속 주행 비율 분석
            if (speed > 1) {
                if (speed < 30) low++;
                else if (speed < 80) middle++;
                else high++;
                activeDriveDurationSec++;
            }
        }

        // 공회전 처리
        if (idlingStartTime != null) {
            idlingPeriods.add(new Drive.StartEndTime(idlingStartTime, stringToInstant(data.get(data.size()-1).get("time"))));
        }
        // 최고 속도 마지막 period는 버린다.
        // 정속 주행 비율 계산
        int total = low + middle + high;
        int lowRate = 100*low/total;
        int middleRate = 100*middle/total;
        int highRate = 100 - lowRate - middleRate;
        speedRate.add(new Drive.SpeedRate("low", lowRate) );
        speedRate.add(new Drive.SpeedRate("middle", middleRate) );
        speedRate.add(new Drive.SpeedRate("high", highRate) );

        drive.setSpeedRate(speedRate);
        drive.setIdlingPeriods(idlingPeriods);
        drive.setSpeedLogs(speedLogs);
        drive.setActiveDriveDurationSec(activeDriveDurationSec);

        return drive;
    }

    private Instant stringToInstant(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);
        ZoneId zone = ZoneId.systemDefault(); // 예: Asia/Seoul
        return localDateTime.atZone(zone).toInstant();
    }
}
