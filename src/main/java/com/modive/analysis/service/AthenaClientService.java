package com.modive.analysis.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AthenaClientService {

    // application.yml에서 주입받을 Athena 데이터베이스
    @Value("${aws.athena.database}")
    private String database;

    // Athena 쿼리 결과를 저장할 S3 위치
    @Value("${aws.athena.output-location}")
    private String outputLocation;

    // Athena 쿼리 실행을 위한 클라이언트 인스턴스
    private final AthenaClient athenaClient = AthenaClient.builder()
            .region(Region.AP_NORTHEAST_2)  // 사용 중인 리전 지정
            .build();

    // S3에서 Athena 결과 CSV를 읽기 위한 클라이언트
    private final S3Client s3Client = S3Client.builder()
            .region(Region.AP_NORTHEAST_2)
            .build();

    /**
     * driveId에 대해 Athena 쿼리를 실행하고 결과를 파싱하여 반환
     * @param driveId 조회할 드라이브 ID
     * @return driveId에 해당하는 주행 로그 레코드 리스트
     */
    public List<Map<String, String>> queryDriveData(String driveId) {
        // Athena에 보낼 SQL 쿼리 생성
        String query = "SELECT * FROM drive WHERE driveId = '" + driveId + "' ORDER BY time";

        // Athena에 쿼리 실행 요청을 보냄
        String executionId = athenaClient.startQueryExecution(
                StartQueryExecutionRequest.builder()
                        .queryString(query)
                        .queryExecutionContext(QueryExecutionContext.builder().database(database).build())  // 사용할 데이터베이스 지정
                        .resultConfiguration(ResultConfiguration.builder()
                                .outputLocation(outputLocation)  // 쿼리 결과를 저장할 S3 경로
                                .build())
                        .build()
        ).queryExecutionId(); // 실행된 쿼리 ID를 받음

        // 쿼리가 끝날 때까지 기다림 (성공/실패 여부 확인)
        waitForQueryToComplete(executionId);

        // Athena 결과 S3 위치 계산: 버킷 이름과 객체 키 생성
        String bucket = outputLocation.replace("s3://", "").split("/")[0]; // 버킷 이름 추출
        String prefix = outputLocation.replace("s3://" + bucket + "/", ""); // 나머지 경로 추출
        String key = prefix + executionId + ".csv"; // 최종 S3 객체 키

        // S3에서 결과 CSV 파일을 읽고 파싱하여 반환
        return parseCsvFromS3(bucket, key);
    }

    /**
     * Athena 쿼리 실행이 완료될 때까지 폴링
     * 실패하거나 취소된 경우 예외 발생
     */
    private void waitForQueryToComplete(String executionId) {
        while (true) {
            // 쿼리 상태 요청
            var result = athenaClient.getQueryExecution(
                    GetQueryExecutionRequest.builder()
                            .queryExecutionId(executionId)
                            .build()
            ).queryExecution().status().state();

            // 성공 시 반복 종료
            if (result == QueryExecutionState.SUCCEEDED) break;

            // 실패 또는 취소 시 예외 처리
            if (result == QueryExecutionState.FAILED || result == QueryExecutionState.CANCELLED) {
                throw new RuntimeException("Athena query failed: " + result);
            }

            // 아직 처리 중인 경우 1초 대기 후 다시 폴링
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    /**
     * S3에서 Athena 쿼리 결과 CSV 파일을 읽고 파싱
     * @param bucket S3 버킷 이름
     * @param key S3 객체 키 (파일 경로)
     * @return 파싱된 레코드 리스트 (각 행은 Map으로 표현)
     */
    private List<Map<String, String>> parseCsvFromS3(String bucket, String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                s3Client.getObject(request), StandardCharsets.UTF_8))) {

            // CSV 파서 설정: 첫 줄을 헤더로 간주
            CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);

            // 결과 저장용 리스트
            List<Map<String, String>> result = new ArrayList<>();

            // CSV 각 행을 Map으로 변환하여 리스트에 추가
            for (CSVRecord record : parser) {
                result.add(record.toMap());
            }

            return result;

        } catch (IOException e) {
            throw new RuntimeException("CSV parsing failed", e);
        }
    }
}

