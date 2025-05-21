package com.modive.analysis.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "event")
@Data
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    private int userId;
    private int driveId;
    private String type;

    @Column(name = "event_time")
    private Instant eventTime;

    private Double gnssX;
    private Double gnssY;
}
