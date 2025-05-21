package com.modive.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DriveEventDto {
    private List<Instant> suddenAccelerations;
    private List<Instant> sharpTurns;
    private List<Instant> laneDepartures;
    private List<Instant> inactiveMoments;
}
