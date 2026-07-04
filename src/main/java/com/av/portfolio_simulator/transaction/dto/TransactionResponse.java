package com.av.portfolio_simulator.transaction.dto;

import com.av.portfolio_simulator.transaction.entity.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response body representing a single transaction in the portfolio history.
 */
@Data
@Builder
public class TransactionResponse {

    private Long id;
    private String symbol;
    private TransactionType type;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private LocalDateTime executedAt;

}
