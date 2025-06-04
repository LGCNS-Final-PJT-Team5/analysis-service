package com.modive.analysis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modive.analysis.dto.EventTotalCntByTypeDTO;
import com.modive.analysis.dto.EventsByDriveDTO;
import com.modive.analysis.dto.EventsByDrivesDTO;
import com.modive.analysis.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest(properties = {"spring.config.location=classpath:application-test.yml"})
@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 특정 주행 ID에 대한 이벤트 발생 횟수 조회 테스트
     */
    @Test
    void shouldReturnEventCountsForSpecificDriveId() throws Exception {
        // Arrange
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        String driveId = "drive1";
        List<EventsByDriveDTO> expectedEvents = Arrays.asList(
                new EventsByDriveDTO("type1", 5L),
                new EventsByDriveDTO("type2", 8L)
        );

        when(eventRepository.countByTypeGroupedByDriveId(eq(driveId))).thenReturn(expectedEvents);

        // Act & Assert
        mockMvc.perform(get("/events/{driveId}", driveId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type", is("type1")))
                .andExpect(jsonPath("$[0].count", is(5)))
                .andExpect(jsonPath("$[1].type", is("type2")))
                .andExpect(jsonPath("$[1].count", is(8)));
    }

    /**
     * 전체 누적 이벤트 발생 횟수 조회 테스트
     */
    @Test
    void shouldReturnTotalEventCounts() throws Exception {
        // Arrange
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        List<EventTotalCntByTypeDTO> expectedTotalEvents = Arrays.asList(
                new EventTotalCntByTypeDTO("type1", 15L),
                new EventTotalCntByTypeDTO("type2", 20L)
        );

        when(eventRepository.totalCntByType()).thenReturn(expectedTotalEvents);

        // Act & Assert
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].reason", is("type1")))
                .andExpect(jsonPath("$[0].count", is(15)))
                .andExpect(jsonPath("$[1].reason", is("type2")))
                .andExpect(jsonPath("$[1].count", is(20)));
    }

    /**
     * 여러 주행 ID에 대한 이벤트 발생 횟수 조회 테스트 - 정상 케이스
     */
    @Test
    void shouldReturnCorrectEventCountsGroupedByDriveIds() throws Exception {
        // Arrange
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        List<String> driveIds = Arrays.asList("drive1", "drive2");

        EventsByDrivesDTO drive1Event1 = new EventsByDrivesDTO("drive1", "type1", 5L);
        EventsByDrivesDTO drive1Event2 = new EventsByDrivesDTO("drive1", "type2", 8L);
        EventsByDrivesDTO drive2Event1 = new EventsByDrivesDTO("drive2", "type1", 3L);

        List<EventsByDrivesDTO> mockResult = Arrays.asList(drive1Event1, drive1Event2, drive2Event1);
        when(eventRepository.countByTypeGroupedByDriveIds(eq(driveIds))).thenReturn(mockResult);

        String requestBody = objectMapper.writeValueAsString(driveIds);

        // Act & Assert
        mockMvc.perform(
                        post("/events/drives")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.drive1", hasSize(2)))
                .andExpect(jsonPath("$.drive1[0].type", is("type1")))
                .andExpect(jsonPath("$.drive1[0].count", is(5)))
                .andExpect(jsonPath("$.drive1[1].type", is("type2")))
                .andExpect(jsonPath("$.drive1[1].count", is(8)))
                .andExpect(jsonPath("$.drive2", hasSize(1)))
                .andExpect(jsonPath("$.drive2[0].type", is("type1")))
                .andExpect(jsonPath("$.drive2[0].count", is(3)));
    }

    /**
     * 빈 주행 ID 목록에 대한 테스트
     */
    @Test
    void shouldReturnEmptyResultWhenNoDrivesProvided() throws Exception {
        // Arrange
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        List<String> emptyDriveIds = Collections.emptyList();
        List<EventsByDrivesDTO> emptyEventList = Collections.emptyList();
        when(eventRepository.countByTypeGroupedByDriveIds(eq(emptyDriveIds))).thenReturn(emptyEventList);

        String requestBody = objectMapper.writeValueAsString(emptyDriveIds);

        // Act & Assert
        mockMvc.perform(
                        post("/events/drives")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));
    }

    /**
     * 존재하지 않는 주행 ID에 대한 테스트
     */
    @Test
    void shouldReturnEmptyResultForNonExistentDriveId() throws Exception {
        // Arrange
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        String nonExistentDriveId = "nonexistent";
        List<EventsByDriveDTO> emptyResult = Collections.emptyList();

        when(eventRepository.countByTypeGroupedByDriveId(eq(nonExistentDriveId))).thenReturn(emptyResult);

        // Act & Assert
        mockMvc.perform(get("/events/{driveId}", nonExistentDriveId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * 잘못된 JSON 형식 요청에 대한 테스트
     */
    @Test
    void shouldReturnBadRequestForInvalidJson() throws Exception {
        // Arrange
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        String invalidRequestBody = "[\"drive1\", \"drive2\""; // 닫는 괄호 누락

        // Act & Assert
        mockMvc.perform(
                        post("/events/drives")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidRequestBody)
                )
                .andExpect(status().isBadRequest());
    }
}