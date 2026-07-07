package com.av.portfolio_simulator.competition.dto;

import com.av.portfolio_simulator.competition.entity.CompetitionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response body representing a competition.
 */
@Data
@Builder
public class CompetitionResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal initialCapital;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private CompetitionStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
    private int participantCount;

}
