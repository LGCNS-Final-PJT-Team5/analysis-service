package com.modive.analysis.repository;

import com.modive.analysis.dto.EventTotalCntByTypeDTO;
import com.modive.analysis.dto.EventsByDriveDTO;
import com.modive.analysis.dto.EventsByDrivesDTO;
import com.modive.analysis.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {

    List<EventEntity> findByDriveId(String driveId);

    // ADMIN
    @Query("""
        SELECT new com.modive.analysis.dto.EventsByDriveDTO(e.type, COUNT(e))
        FROM EventEntity e
        WHERE e.driveId = :driveId
        GROUP BY e.type
    """)
    List<EventsByDriveDTO> countByTypeGroupedByDriveId(@Param("driveId") String driveId);

    @Query("""
        SELECT new com.modive.analysis.dto.EventTotalCntByTypeDTO(e.type, COUNT(e))
        FROM EventEntity e
        GROUP BY e.type
    """)
    List<EventTotalCntByTypeDTO> totalCntByType();

    @Query("""
        SELECT new com.modive.analysis.dto.EventsByDrivesDTO(e.driveId, e.type, COUNT(e))
        FROM EventEntity e WHERE e.driveId IN :driveIds GROUP BY e.driveId, e.type
    """)
    List<EventsByDrivesDTO> countByTypeGroupedByDriveIds(@Param("driveIds") List<String> driveIds);



}