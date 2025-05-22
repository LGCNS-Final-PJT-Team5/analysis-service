package com.modive.analysis.service;

import com.modive.analysis.entity.Drive;
import com.modive.analysis.entity.EventEntity;
import com.modive.analysis.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventDataService {

    private final EventRepository eventRepository;

    public Drive loadDriveData(String driveId) {
        List<EventEntity> events = eventRepository.findByDriveId(driveId);

        if (events.isEmpty()) {
            throw new RuntimeException("No events found for driveId: " + driveId);
        }

        Drive drive = new Drive();
        drive.setDriveId(driveId);

        List<Instant> suddenAccelerations = new ArrayList<>();
        List<Instant> sharpTurns = new ArrayList<>();
        List<Instant> laneDepartures = new ArrayList<>();
        List<Instant> inactiveMoments = new ArrayList<>();

        for (EventEntity event : events) {
            Instant time = event.getEventTime();
            switch (event.getType()) {
                case "RAPID_ACCELERATION", "RAPID_DECELERATION" -> suddenAccelerations.add(time);
                case "SHARP_TURN" -> sharpTurns.add(time);
                case "LANE_DEPARTURE" -> laneDepartures.add(time);
                case "NO_OPERATION" -> inactiveMoments.add(time);
            }
        }

        drive.setSuddenAccelerations(suddenAccelerations);
        drive.setSharpTurns(sharpTurns);
        drive.setLaneDepartures(laneDepartures);
        drive.setInactiveMoments(inactiveMoments);
        return drive;
    }

    private EventEntity createMockEvent(String type, String timeStr) {
        EventEntity event = new EventEntity();
        event.setType(type);
        event.setEventTime(Instant.parse(timeStr + "Z")); // Instant는 Z(UTC) 붙여야 파싱됨
        event.setUserId(99);
        event.setDriveId("99");
        event.setGnssX(127.0);
        event.setGnssY(37.0);
        return event;
    }
}
