package com.modive.analysis.controller;

import com.modive.analysis.dto.Drive;
import com.modive.analysis.dto.DriveEventDto;
import com.modive.analysis.service.EventDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/drive")
public class EventController {

    private final EventDataService eventDataService;

    @GetMapping("/{driveId}")
    public ResponseEntity<DriveEventDto> getDrive(@PathVariable String driveId) {
        Drive drive = eventDataService.loadDriveData(driveId);
        DriveEventDto dto = new DriveEventDto(
                drive.getSuddenAccelerations(),
                drive.getSharpTurns(),
                drive.getLaneDepartures(),
                drive.getInactiveMoments()
        );
        return ResponseEntity.ok(dto);
    }
}