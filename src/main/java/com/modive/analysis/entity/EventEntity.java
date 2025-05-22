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
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "drive_id")
    private int driveId;

    private String type;

    @Column(name = "event_time")
    private Instant eventTime;

    @Column(name = "gnss_x")
    private Double gnssX;

    @Column(name = "gnss_y")
    private Double gnssY;
}
