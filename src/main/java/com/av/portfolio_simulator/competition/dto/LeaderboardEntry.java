package com.av.portfolio_simulator.competition.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * A single entry in the competition leaderboard.
 * Sorted by totalReturnPercent descending.
 */
@Data
@Builder
public class LeaderboardEntry {

    private int rank;
    private String username;
    private BigDecimal initialCapital;
    private BigDecimal currentValue;
    private BigDecimal totalReturn;
    private BigDecimal totalReturnPercent;

}
