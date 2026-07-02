package com.av.portfolio_simulator.portfolio.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response body representing a simulated portfolio.
 * Includes computed metrics such as total value and total return.
 */
@Data
@Builder
public class PortfolioResponse {

    private Long id;
    private String name;
    private BigDecimal initialCapital;
    private BigDecimal cashBalance;

    // Sum of all current holding values (quantity * current price)
    // Populated as 0 until the holdings module is implemented
    private BigDecimal holdingsValue;

    // cashBalance + holdingsValue;
    private BigDecimal totalValue;

    // totalValue - initialCapital
    private BigDecimal totalReturn;

    // ((totalValue - initialCapital) / initialCapital) * 100
    private BigDecimal totalReturnPercent;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
