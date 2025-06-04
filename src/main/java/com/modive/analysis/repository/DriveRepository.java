package com.modive.analysis.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.modive.analysis.entity.Drive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;


@Slf4j
@Repository
@RequiredArgsConstructor
public class DriveRepository {

    private final DynamoDBMapper dynamoDBMapper;

    public void save(Drive drive) {
        dynamoDBMapper.save(drive);
    }

}