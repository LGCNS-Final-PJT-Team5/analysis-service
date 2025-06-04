package com.modive.analysis.service;

import com.modive.analysis.entity.Drive;
import com.modive.analysis.entity.EventEntity;
import com.modive.analysis.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventDataServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventDataService eventDataService;

    private String testDriveId;
    private Instant baseTime;

    @BeforeEach
    void setUp() {
        testDriveId = "test-drive-123";
        baseTime = Instant.parse("2025-05-31T10:00:00Z");
    }

    @Test
    public void testLoadDriveDataWithMixedEvents() {
        // Arrange
        List<EventEntity> mockEvents = createMixedEvents();
        when(eventRepository.findByDriveId(testDriveId)).thenReturn(mockEvents);

        // Act
        Drive result = eventDataService.loadDriveData(testDriveId);

        // Assert
        assertNotNull(result);
        assertEquals(testDriveId, result.getDriveId());

        // 급가속/급감속 이벤트 검증 (RAPID_ACCELERATION, RAPID_DECELERATION)
        assertEquals(2, result.getSuddenAccelerations().size());
        assertTrue(result.getSuddenAccelerations().contains(baseTime));
        assertTrue(result.getSuddenAccelerations().contains(baseTime.plusSeconds(60)));

        // 급회전 이벤트 검증 (SHARP_TURN)
        assertEquals(1, result.getSharpTurns().size());
        assertTrue(result.getSharpTurns().contains(baseTime.plusSeconds(120)));

        // 차선 이탈 이벤트 검증 (LANE_DEPARTURE)
        assertEquals(1, result.getLaneDepartures().size());
        assertTrue(result.getLaneDepartures().contains(baseTime.plusSeconds(180)));

        // 비활성 모멘트 검증 (NO_OPERATION)
        assertEquals(1, result.getInactiveMoments().size());
        assertTrue(result.getInactiveMoments().contains(baseTime.plusSeconds(240)));

        verify(eventRepository, times(1)).findByDriveId(testDriveId);
    }

    @Test
    public void testLoadDriveDataWithRapidAccelerationOnly() {
        // Arrange
        List<EventEntity> mockEvents = new ArrayList<>();
        mockEvents.add(createEventEntity("RAPID_ACCELERATION", baseTime));
        mockEvents.add(createEventEntity("RAPID_ACCELERATION", baseTime.plusSeconds(30)));

        when(eventRepository.findByDriveId(testDriveId)).thenReturn(mockEvents);

        // Act
        Drive result = eventDataService.loadDriveData(testDriveId);

        // Assert
        assertNotNull(result);
        assertEquals(testDriveId, result.getDriveId());
        assertEquals(2, result.getSuddenAccelerations().size());
        assertEquals(0, result.getSharpTurns().size());
        assertEquals(0, result.getLaneDepartures().size());
        assertEquals(0, result.getInactiveMoments().size());

        verify(eventRepository, times(1)).findByDriveId(testDriveId);
    }

    @Test
    public void testLoadDriveDataWithRapidDecelerationOnly() {
        // Arrange
        List<EventEntity> mockEvents = new ArrayList<>();
        mockEvents.add(createEventEntity("RAPID_DECELERATION", baseTime));
        mockEvents.add(createEventEntity("RAPID_DECELERATION", baseTime.plusSeconds(45)));

        when(eventRepository.findByDriveId(testDriveId)).thenReturn(mockEvents);

        // Act
        Drive result = eventDataService.loadDriveData(testDriveId);

        // Assert
        assertNotNull(result);
        assertEquals(testDriveId, result.getDriveId());
        assertEquals(2, result.getSuddenAccelerations().size()); // RAPID_DECELERATION도 suddenAccelerations에 포함
        assertEquals(0, result.getSharpTurns().size());
        assertEquals(0, result.getLaneDepartures().size());
        assertEquals(0, result.getInactiveMoments().size());

        verify(eventRepository, times(1)).findByDriveId(testDriveId);
    }

    @Test
    public void testLoadDriveDataWithSharpTurnsOnly() {
        // Arrange
        List<EventEntity> mockEvents = new ArrayList<>();
        mockEvents.add(createEventEntity("SHARP_TURN", baseTime));
        mockEvents.add(createEventEntity("SHARP_TURN", baseTime.plusSeconds(90)));
        mockEvents.add(createEventEntity("SHARP_TURN", baseTime.plusSeconds(150)));

        when(eventRepository.findByDriveId(testDriveId)).thenReturn(mockEvents);

        // Act
        Drive result = eventDataService.loadDriveData(testDriveId);

        // Assert
        assertNotNull(result);
        assertEquals(testDriveId, result.getDriveId());
        assertEquals(0, result.getSuddenAccelerations().size());
        assertEquals(3, result.getSharpTurns().size());
        assertEquals(0, result.getLaneDepartures().size());
        assertEquals(0, result.getInactiveMoments().size());

        verify(eventRepository, times(1)).findByDriveId(testDriveId);
    }

    @Test
    public void testLoadDriveDataWithLaneDeparturesOnly() {
        // Arrange
        List<EventEntity> mockEvents = new ArrayList<>();
        mockEvents.add(createEventEntity("LANE_DEPARTURE", baseTime));
        mockEvents.add(createEventEntity("LANE_DEPARTURE", baseTime.plusSeconds(120)));

        when(eventRepository.findByDriveId(testDriveId)).thenReturn(mockEvents);

        // Act
        Drive result = eventDataService.loadDriveData(testDriveId);

        // Assert
        assertNotNull(result);
        assertEquals(testDriveId, result.getDriveId());
        assertEquals(0, result.getSuddenAccelerations().size());
        assertEquals(0, result.getSharpTurns().size());
        assertEquals(2, result.getLaneDepartures().size());
        assertEquals(0, result.getInactiveMoments().size());

        verify(eventRepository, times(1)).findByDriveId(testDriveId);
    }

    @Test
    public void testLoadDriveDataWithNoOperationOnly() {
        // Arrange
        List<EventEntity> mockEvents = new ArrayList<>();
        mockEvents.add(createEventEntity("NO_OPERATION", baseTime));
        mockEvents.add(createEventEntity("NO_OPERATION", baseTime.plusSeconds(300)));
        mockEvents.add(createEventEntity("NO_OPERATION", baseTime.plusSeconds(600)));

        when(eventRepository.findByDriveId(testDriveId)).thenReturn(mockEvents);

        // Act
        Drive result = eventDataService.loadDriveData(testDriveId);

        // Assert
        assertNotNull(result);
        assertEquals(testDriveId, result.getDriveId());
        assertEquals(0, result.getSuddenAccelerations().size());
        assertEquals(0, result.getSharpTurns().size());
        assertEquals(0, result.getLaneDepartures().size());
        assertEquals(3, result.getInactiveMoments().size());

        verify(eventRepository, times(1)).findByDriveId(testDriveId);
    }

    @Test
    public void testLoadDriveDataWithUnknownEventTypes() {
        // Arrange
        List<EventEntity> mockEvents = new ArrayList<>();
        mockEvents.add(createEventEntity("UNKNOWN_EVENT", baseTime));
        mockEvents.add(createEventEntity("INVALID_TYPE", baseTime.plusSeconds(60)));
        mockEvents.add(createEventEntity("RAPID_ACCELERATION", baseTime.plusSeconds(120))); // 알려진 타입 1개

        when(eventRepository.findByDriveId(testDriveId)).thenReturn(mockEvents);

        // Act
        Drive result = eventDataService.loadDriveData(testDriveId);

        // Assert
        assertNotNull(result);
        assertEquals(testDriveId, result.getDriveId());
        assertEquals(1, result.getSuddenAccelerations().size()); // 알려진 타입만 처리됨
        assertEquals(0, result.getSharpTurns().size());
        assertEquals(0, result.getLaneDepartures().size());
        assertEquals(0, result.getInactiveMoments().size());

        verify(eventRepository, times(1)).findByDriveId(testDriveId);
    }

    @Test
    public void testLoadDriveDataWithEmptyEventList() {
        // Arrange
        when(eventRepository.findByDriveId(testDriveId)).thenReturn(new ArrayList<>());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventDataService.loadDriveData(testDriveId);
        });

        assertEquals("No events found for driveId: " + testDriveId, exception.getMessage());
        verify(eventRepository, times(1)).findByDriveId(testDriveId);
    }

    @Test
    public void testLoadDriveDataWithNullDriveId() {
        // Arrange
        String nullDriveId = null;
        when(eventRepository.findByDriveId(nullDriveId)).thenReturn(new ArrayList<>());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventDataService.loadDriveData(nullDriveId);
        });

        assertTrue(exception.getMessage().contains("No events found for driveId: null"));
        verify(eventRepository, times(1)).findByDriveId(nullDriveId);
    }

    @Test
    public void testLoadDriveDataWithRepositoryException() {
        // Arrange
        when(eventRepository.findByDriveId(testDriveId))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventDataService.loadDriveData(testDriveId);
        });

        assertEquals("Database connection failed", exception.getMessage());
        verify(eventRepository, times(1)).findByDriveId(testDriveId);
    }

    @Test
    public void testLoadDriveDataWithSameTimeEvents() {
        // Arrange - 같은 시간에 여러 이벤트 발생
        Instant sameTime = baseTime;
        List<EventEntity> mockEvents = new ArrayList<>();
        mockEvents.add(createEventEntity("RAPID_ACCELERATION", sameTime));
        mockEvents.add(createEventEntity("SHARP_TURN", sameTime));
        mockEvents.add(createEventEntity("LANE_DEPARTURE", sameTime));

        when(eventRepository.findByDriveId(testDriveId)).thenReturn(mockEvents);

        // Act
        Drive result = eventDataService.loadDriveData(testDriveId);

        // Assert
        assertNotNull(result);
        assertEquals(testDriveId, result.getDriveId());
        assertEquals(1, result.getSuddenAccelerations().size());
        assertEquals(1, result.getSharpTurns().size());
        assertEquals(1, result.getLaneDepartures().size());
        assertEquals(0, result.getInactiveMoments().size());

        // 모든 이벤트가 같은 시간에 발생했는지 확인
        assertTrue(result.getSuddenAccelerations().contains(sameTime));
        assertTrue(result.getSharpTurns().contains(sameTime));
        assertTrue(result.getLaneDepartures().contains(sameTime));

        verify(eventRepository, times(1)).findByDriveId(testDriveId);
    }

    @Test
    public void testLoadDriveDataWithLargeNumberOfEvents() {
        // Arrange - 대량의 이벤트 데이터
        List<EventEntity> mockEvents = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            String eventType = (i % 4 == 0) ? "RAPID_ACCELERATION" :
                    (i % 4 == 1) ? "SHARP_TURN" :
                            (i % 4 == 2) ? "LANE_DEPARTURE" : "NO_OPERATION";
            mockEvents.add(createEventEntity(eventType, baseTime.plusSeconds(i)));
        }

        when(eventRepository.findByDriveId(testDriveId)).thenReturn(mockEvents);

        // Act
        Drive result = eventDataService.loadDriveData(testDriveId);

        // Assert
        assertNotNull(result);
        assertEquals(testDriveId, result.getDriveId());
        assertEquals(250, result.getSuddenAccelerations().size()); // 1000 / 4
        assertEquals(250, result.getSharpTurns().size());
        assertEquals(250, result.getLaneDepartures().size());
        assertEquals(250, result.getInactiveMoments().size());

        verify(eventRepository, times(1)).findByDriveId(testDriveId);
    }

    // Helper 메서드들
    private List<EventEntity> createMixedEvents() {
        List<EventEntity> events = new ArrayList<>();
        events.add(createEventEntity("RAPID_ACCELERATION", baseTime));
        events.add(createEventEntity("RAPID_DECELERATION", baseTime.plusSeconds(60)));
        events.add(createEventEntity("SHARP_TURN", baseTime.plusSeconds(120)));
        events.add(createEventEntity("LANE_DEPARTURE", baseTime.plusSeconds(180)));
        events.add(createEventEntity("NO_OPERATION", baseTime.plusSeconds(240)));
        return events;
    }

    private EventEntity createEventEntity(String type, Instant eventTime) {
        EventEntity event = new EventEntity();
        event.setType(type);
        event.setEventTime(eventTime);
        event.setDriveId(testDriveId);
        return event;
    }
}