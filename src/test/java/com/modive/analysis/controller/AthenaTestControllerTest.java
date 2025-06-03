package com.modive.analysis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modive.analysis.entity.Drive;
import com.modive.analysis.service.AnalysisDataFromAthenaService;
import com.modive.analysis.service.AthenaClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AthenaTestControllerTest {

    @Mock
    private AthenaClientService athenaClientService;

    @Mock
    private AnalysisDataFromAthenaService analysisDataFromAthenaService;

    @InjectMocks
    private AthenaTestController athenaTestController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testAthenaQuery_Success() throws Exception {
        // Arrange
        mockMvc = MockMvcBuilders.standaloneSetup(athenaTestController).build();

        String driveId = "drive123";
        List<Map<String, String>> queryData = List.of(
                Map.of("key", "value")
        );

        Drive mockDrive = new Drive();
        mockDrive.setStartTime(Instant.now());
        mockDrive.setEndTime(Instant.now());
        mockDrive.setSuddenAccelerations(List.of(Instant.now()));

        when(athenaClientService.queryDriveData(anyString())).thenReturn(queryData);
        when(analysisDataFromAthenaService.analysisData(Mockito.anyList())).thenReturn(mockDrive);

        // Act & Assert
        mockMvc.perform(get("/test-athena/{driveId}", driveId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime").isNotEmpty())
                .andExpect(jsonPath("$.endTime").isNotEmpty())
                .andExpect(jsonPath("$.suddenAccelerations").isArray());
    }

    @Test
    public void testAthenaQueryAll_Success() throws Exception {
        // Arrange
        mockMvc = MockMvcBuilders.standaloneSetup(athenaTestController).build();

        String driveId = "drive123";
        List<Map<String, String>> queryData = List.of(
                Map.of("key1", "value1"),
                Map.of("key2", "value2")
        );

        when(athenaClientService.queryDriveData(anyString())).thenReturn(queryData);

        // Act & Assert
        mockMvc.perform(get("/test-athena/all/{driveId}", driveId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].key1", is("value1")))
                .andExpect(jsonPath("$[1].key2", is("value2")));
    }

    @Test
    public void testAthenaQuery_EmptyResult() throws Exception {
        // Arrange
        mockMvc = MockMvcBuilders.standaloneSetup(athenaTestController).build();

        String driveId = "drive123";
        List<Map<String, String>> emptyQueryData = List.of();

        Drive emptyDrive = new Drive();

        when(athenaClientService.queryDriveData(anyString())).thenReturn(emptyQueryData);
        when(analysisDataFromAthenaService.analysisData(Mockito.anyList())).thenReturn(emptyDrive);

        // Act & Assert
        mockMvc.perform(get("/test-athena/{driveId}", driveId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testAthenaQueryAll_EmptyResult() throws Exception {
        // Arrange
        mockMvc = MockMvcBuilders.standaloneSetup(athenaTestController).build();

        String driveId = "drive123";
        List<Map<String, String>> emptyQueryData = List.of();

        when(athenaClientService.queryDriveData(anyString())).thenReturn(emptyQueryData);

        // Act & Assert
        mockMvc.perform(get("/test-athena/all/{driveId}", driveId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

}