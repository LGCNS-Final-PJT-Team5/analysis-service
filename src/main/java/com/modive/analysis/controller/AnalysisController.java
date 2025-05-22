package com.modive.analysis.controller;

import com.modive.analysis.client.DashboardClient;
import com.modive.analysis.dto.Drive;
import com.modive.analysis.service.AnalysisDataFromAthenaService;
import com.modive.analysis.service.AthenaClientService;
import com.modive.analysis.service.EventDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/analysis")
public class AnalysisController {

    private final AthenaClientService athenaClientService;
    private final AnalysisDataFromAthenaService analysisDataFromAthenaService;
    private final EventDataService eventDataService;
    private final DashboardClient dashboardClient;

    @GetMapping("/{driveId}")
    public void postDriveAnalysis(@PathVariable String driveId) {


        List<Map<String, String>> data = athenaClientService.queryDriveData(driveId);
        Drive result1 = analysisDataFromAthenaService.analysisData(data);
        Drive result2 = eventDataService.loadDriveData(driveId);

        Drive finalResult = mergeDriveResults(result1, result2);

        // dynamodb에 저장
    }

    public Drive mergeDriveResults(Drive d1, Drive d2) {
        Drive result = new Drive();
        for (Field field : Drive.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value1 = field.get(d1);
                Object value2 = field.get(d2);
                field.set(result, value1 != null ? value1 : value2);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

}
