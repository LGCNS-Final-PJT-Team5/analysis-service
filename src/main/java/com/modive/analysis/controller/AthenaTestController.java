package com.modive.analysis.controller;

import com.modive.analysis.dto.Drive;
import com.modive.analysis.service.AnalysisDataFromAthenaService;
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
    private final AnalysisDataFromAthenaService analysisDataFromAthenaService;

    @GetMapping("/{driveId}")
    public ResponseEntity<Drive> testAthenaQuery(@PathVariable String driveId) {

        List<Map<String, String>> data = athenaClientService.queryDriveData(driveId);
        Drive result = analysisDataFromAthenaService.analysisData(data);

        return ResponseEntity.ok(result);
    }
}
