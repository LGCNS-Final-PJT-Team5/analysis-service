package com.modive.analysis.controller;

import com.modive.analysis.entity.Drive;
import com.modive.analysis.service.EventDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/drive")
public class EventController {

    private final EventDataService eventDataService;

    @GetMapping("/{driveId}")
    public ResponseEntity<Drive> getDrive(@PathVariable String driveId) {
        Drive drive = eventDataService.loadDriveData(driveId);

        return ResponseEntity.ok(drive);
    }
}