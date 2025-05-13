package com.modive.analysis.controller;

import com.modive.analysis.service.AthenaClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test-athena")
public class AthenaTestController {

    private final AthenaClientService athenaClientService;

    @GetMapping("/{driveId}")
    public ResponseEntity<List<Map<String, String>>> testAthenaQuery(@PathVariable String driveId) {
        List<Map<String, String>> result = athenaClientService.queryDriveData(driveId);
        return ResponseEntity.ok(result);
    }
}
