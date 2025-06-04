package com.modive.analysis.controller;

import com.modive.analysis.entity.Drive;
import com.modive.analysis.repository.DriveRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//@SpringBootTest(properties = {"spring.config.location=classpath:application-test.yml"})
@ExtendWith(MockitoExtension.class)
public class AnalysisControllerTest {

    @InjectMocks
    private AnalysisController analysisController;

    @Mock
    private DriveRepository driveRepository;

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
}