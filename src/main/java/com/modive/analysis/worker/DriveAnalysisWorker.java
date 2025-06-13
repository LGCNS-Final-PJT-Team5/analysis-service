package com.modive.analysis.worker;

import com.modive.analysis.entity.Drive;
import com.modive.analysis.repository.DriveRepository;
import com.modive.analysis.service.AnalysisDataFromAthenaService;
import com.modive.analysis.service.AthenaClientService;
import com.modive.analysis.service.EventDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@RequiredArgsConstructor
public class DriveAnalysisWorker implements InitializingBean {

    private final BlockingQueue<String> driveQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(20); // 병렬처리 수 조정

    private final AthenaClientService athenaClientService;
    private final AnalysisDataFromAthenaService analysisDataFromAthenaService;
    private final EventDataService eventDataService;
    private final DriveRepository driveRepository;

    @Override
    public void afterPropertiesSet() {
        for (int i = 0; i < 20; i++) {
            executor.submit(() -> {
                while (true) {
                    try {
                        String driveId = driveQueue.take(); // 큐에서 blocking으로 대기
                        process(driveId); // Athena + RDS 분석 + 저장 등
                    } catch (Exception e) {
                        System.out.println("Error processing drive analysis" + e);
                    }
                }
            });
        }
    }

    public void enqueue(String driveId) {
        driveQueue.offer(driveId); // controller에서 이 메서드를 호출
    }

    private void process(String driveId) {
        // 분석 로직 호출
        List<Map<String, String>> data = athenaClientService.queryDriveData(driveId);
        Drive result1 = analysisDataFromAthenaService.analysisData(data); // Athena로 S3의 데이터 쿼리
        Drive result2 = eventDataService.loadDriveData(driveId); // JPA로 RDS(MySQL) 데이터 쿼리

        Drive finalResult = mergeDriveResults(result1, result2);

        // dynamodb에 저장
        driveRepository.save(finalResult);
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
