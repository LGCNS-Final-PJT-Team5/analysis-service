package com.modive.analysis.controller;

import com.modive.analysis.dto.EventTotalCntByTypeDTO;
import com.modive.analysis.dto.EventsByDriveDTO;
import com.modive.analysis.entity.Drive;
import com.modive.analysis.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class AdminController {

    private final EventRepository eventRepository;

    /**
     * 특정 주행의 이벤트별 발생 횟수 계산
     *
     * @param driveId 운전 ID
     * @return 특정 주행의 이벤트별 발생 횟수
     */
    @GetMapping("/{driveId}")
    public List<EventsByDriveDTO> getTotalEventCntByType(@PathVariable String driveId) {
        return eventRepository.countByTypeGroupedByDriveId(driveId);
    }

    /**
     * 전체 누적 이벤트 발생 횟수 계산
     *
     * @return 전체 누적 이벤트 발생 횟수
     */
    @GetMapping()
    public List<EventTotalCntByTypeDTO> getTotalEventCntByType() {
        return eventRepository.totalCntByType();
    }

    /**
     * 특정 주행의 이벤트별 발생 횟수 계산 (리스트)
     *
     * @param driveIds 주행 ID 목록
     * @return 주행 ID 별 이벤트 발생 횟수 목록
     */
    @PostMapping("/drives")
    public Map<String, List<EventsByDriveDTO>> getTotalEventCntByDrive(@RequestBody List<String> driveIds) {

        Map<String, List<EventsByDriveDTO>> result = new HashMap<>();

        for (String driveId : driveIds) {
            List<EventsByDriveDTO> list = eventRepository.countByTypeGroupedByDriveId(driveId);
            result.put(driveId, list);
        }

        return result;
    }
}
