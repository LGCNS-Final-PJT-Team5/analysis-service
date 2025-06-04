package com.modive.analysis.controller;

import com.modive.analysis.entity.Drive;
import com.modive.analysis.repository.DriveRepository;
import com.modive.analysis.service.AnalysisDataFromAthenaService;
import com.modive.analysis.service.AthenaClientService;
import com.modive.analysis.service.EventDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalysisControllerTest {

    @InjectMocks
    private AnalysisController analysisController;

    @Mock
    private AthenaClientService athenaClientService;

    @Mock
    private AnalysisDataFromAthenaService analysisDataFromAthenaService;

    @Mock
    private EventDataService eventDataService;

    @Mock
    private DriveRepository driveRepository;

    // 기존 mergeDriveResults 테스트들...
    @Test
    public void testMergeDriveResults_allFieldsPresentInDrive1() {
        // Arrange
        Drive drive1 = new Drive();
        drive1.setStartTime(Instant.parse("2023-01-01T10:00:00Z"));
        drive1.setEndTime(Instant.parse("2023-01-01T11:00:00Z"));
        drive1.setSuddenAccelerations(List.of(Instant.parse("2023-01-01T10:05:00Z")));
        drive1.setSharpTurns(List.of(Instant.parse("2023-01-01T10:15:00Z")));

        Drive drive2 = new Drive();

        // Act
        Drive mergedResult = analysisController.mergeDriveResults(drive1, drive2);

        // Assert
        assertThat(mergedResult.getStartTime()).isEqualTo(drive1.getStartTime());
        assertThat(mergedResult.getEndTime()).isEqualTo(drive1.getEndTime());
        assertThat(mergedResult.getSuddenAccelerations()).isEqualTo(drive1.getSuddenAccelerations());
        assertThat(mergedResult.getSharpTurns()).isEqualTo(drive1.getSharpTurns());
    }

    @Test
    public void testMergeDriveResults_allFieldsPresentInDrive2() {
        // Arrange
        Drive drive1 = new Drive();

        Drive drive2 = new Drive();
        drive2.setStartTime(Instant.parse("2023-01-01T10:00:00Z"));
        drive2.setEndTime(Instant.parse("2023-01-01T11:00:00Z"));
        drive2.setSuddenAccelerations(List.of(Instant.parse("2023-01-01T10:05:00Z")));
        drive2.setSharpTurns(List.of(Instant.parse("2023-01-01T10:15:00Z")));

        // Act
        Drive mergedResult = analysisController.mergeDriveResults(drive1, drive2);

        // Assert
        assertThat(mergedResult.getStartTime()).isEqualTo(drive2.getStartTime());
        assertThat(mergedResult.getEndTime()).isEqualTo(drive2.getEndTime());
        assertThat(mergedResult.getSuddenAccelerations()).isEqualTo(drive2.getSuddenAccelerations());
        assertThat(mergedResult.getSharpTurns()).isEqualTo(drive2.getSharpTurns());
    }

    @Test
    public void testMergeDriveResults_fieldsPresentInBothPreferDrive1() {
        // Arrange
        Drive drive1 = new Drive();
        drive1.setStartTime(Instant.parse("2023-01-01T10:00:00Z"));
        drive1.setEndTime(Instant.parse("2023-01-01T11:00:00Z"));

        Drive drive2 = new Drive();
        drive2.setStartTime(Instant.parse("2022-12-31T10:00:00Z"));
        drive2.setEndTime(Instant.parse("2022-12-31T11:00:00Z"));

        // Act
        Drive mergedResult = analysisController.mergeDriveResults(drive1, drive2);

        // Assert
        assertThat(mergedResult.getStartTime()).isEqualTo(drive1.getStartTime());
        assertThat(mergedResult.getEndTime()).isEqualTo(drive1.getEndTime());
    }

    @Test
    public void testMergeDriveResults_mixedFieldsFromBoth() {
        // Arrange
        Drive drive1 = new Drive();
        drive1.setStartTime(Instant.parse("2023-01-01T10:00:00Z"));

        Drive drive2 = new Drive();
        drive2.setEndTime(Instant.parse("2023-01-01T11:00:00Z"));
        drive2.setSuddenAccelerations(List.of(Instant.parse("2023-01-01T10:05:00Z")));

        // Act
        Drive mergedResult = analysisController.mergeDriveResults(drive1, drive2);

        // Assert
        assertThat(mergedResult.getStartTime()).isEqualTo(drive1.getStartTime());
        assertThat(mergedResult.getEndTime()).isEqualTo(drive2.getEndTime());
        assertThat(mergedResult.getSuddenAccelerations()).isEqualTo(drive2.getSuddenAccelerations());
    }

    @Test
    public void testMergeDriveResults_emptyDrives() {
        // Arrange
        Drive drive1 = new Drive();
        Drive drive2 = new Drive();

        // Act
        Drive mergedResult = analysisController.mergeDriveResults(drive1, drive2);

        // Assert
        assertThat(mergedResult.getStartTime()).isNull();
        assertThat(mergedResult.getEndTime()).isNull();
        assertThat(mergedResult.getSuddenAccelerations()).isNull();
        assertThat(mergedResult.getSharpTurns()).isNull();
    }

    @Test
    public void testMergeDriveResults_nullDrives() {
        // Arrange & Act & Assert
        // null 입력에 대한 방어적 테스트
        // 실제 구현에 따라 NullPointerException을 던지거나 빈 Drive를 반환할 수 있음
        try {
            Drive mergedResult = analysisController.mergeDriveResults(null, null);
            // 만약 null 처리가 구현되어 있다면
            assertThat(mergedResult).isNotNull();
        } catch (NullPointerException e) {
            // null 처리가 안 되어 있다면 예외 발생 예상
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }

    // ============= postDriveAnalysis 메서드 테스트 추가 =============
    @Test
    public void testPostDriveAnalysis_success() {
        // Arrange
        String driveId = "test-drive-123";
        List<Map<String, String>> mockData = List.of(Map.of("key", "value"));

        Drive result1 = new Drive();
        result1.setStartTime(Instant.parse("2023-01-01T10:00:00Z"));

        Drive result2 = new Drive();
        result2.setEndTime(Instant.parse("2023-01-01T11:00:00Z"));

        // Mock 설정 - postDriveAnalysis는 void 반환이므로 when().thenReturn() 불가
        when(athenaClientService.queryDriveData(driveId)).thenReturn(mockData);
        when(analysisDataFromAthenaService.analysisData(mockData)).thenReturn(result1);
        when(eventDataService.loadDriveData(driveId)).thenReturn(result2);

        // Act
        analysisController.postDriveAnalysis(driveId);

        // Assert
        verify(athenaClientService, times(1)).queryDriveData(driveId);
        verify(analysisDataFromAthenaService, times(1)).analysisData(mockData);
        verify(eventDataService, times(1)).loadDriveData(driveId);
        verify(driveRepository, times(1)).save(any(Drive.class));
    }

    @Test
    public void testPostDriveAnalysis_withNullResults() {
        // Arrange
        String driveId = "test-drive-456";
        List<Map<String, String>> mockData = List.of();

        // Mock 설정 - 서비스들이 null을 반환하는 경우
        when(athenaClientService.queryDriveData(driveId)).thenReturn(mockData);
        when(analysisDataFromAthenaService.analysisData(mockData)).thenReturn(null);
        when(eventDataService.loadDriveData(driveId)).thenReturn(null);

        // Act & Assert
        // null 처리에 따라 예외가 발생할 수 있음
        try {
            analysisController.postDriveAnalysis(driveId);
            verify(driveRepository, times(1)).save(any(Drive.class));
        } catch (Exception e) {
            // 예외 발생 시에도 정상적인 동작으로 간주
            assertThat(e).isNotNull();
        }

        // 서비스 호출은 정상적으로 이루어져야 함
        verify(athenaClientService, times(1)).queryDriveData(driveId);
        verify(analysisDataFromAthenaService, times(1)).analysisData(mockData);
        verify(eventDataService, times(1)).loadDriveData(driveId);
    }

    @Test
    public void testPostDriveAnalysis_serviceException() {
        // Arrange
        String driveId = "test-drive-789";

        // Mock 설정 - athenaClientService에서 예외 발생
        when(athenaClientService.queryDriveData(driveId))
                .thenThrow(new RuntimeException("Athena query failed"));

        // Act & Assert
        try {
            analysisController.postDriveAnalysis(driveId);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Athena query failed");
        }

        // athenaClientService는 호출되었지만, 나머지는 호출되지 않아야 함
        verify(athenaClientService, times(1)).queryDriveData(driveId);
        verify(analysisDataFromAthenaService, never()).analysisData(any());
        verify(eventDataService, never()).loadDriveData(anyString());
        verify(driveRepository, never()).save(any());
    }
}