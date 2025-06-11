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

//        if (events.isEmpty()) {
//            throw new RuntimeException("No events found for driveId: " + driveId);
//        }

        Drive drive = new Drive();
        drive.setDriveId(driveId);

        List<Instant> suddenAccelerations = new ArrayList<>();
        List<Instant> sharpTurns = new ArrayList<>();
        List<Instant> laneDepartures = new ArrayList<>();
        List<Instant> inactiveMoments = new ArrayList<>();

        List<Instant> reactionTimes = new ArrayList<>();
        List<Instant> followingDistanceEvents = new ArrayList<>();

        for (EventEntity event : events) {
            Instant time = event.getEventTime();
            switch (event.getType()) {
                case "급가속", "급감속" -> suddenAccelerations.add(time);
                case "급회전" -> sharpTurns.add(time);
                case "차선 이탈" -> laneDepartures.add(time);
                case "미조작" -> inactiveMoments.add(time);
                case "반응속도 미흡" -> reactionTimes.add(time);
                case "안전 거리 미준수" -> followingDistanceEvents.add(time);
            }
        }

        drive.setSuddenAccelerations(suddenAccelerations);
        drive.setSharpTurns(sharpTurns);
        drive.setLaneDepartures(laneDepartures);
        drive.setInactiveMoments(inactiveMoments);

        drive.setReactionTimes(reactionTimes);
        drive.setFollowingDistanceEvents(followingDistanceEvents);

        return drive;
    }

}
