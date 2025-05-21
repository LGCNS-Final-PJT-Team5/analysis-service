package com.modive.analysis.service;

import com.modive.analysis.dto.Drive;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AnalysisDataFromAthenaService {

    public Drive analysisData(List<Map<String, String>> data) {

        Drive drive = new Drive();

        // 여기서 분석

        return drive;
    }
}
