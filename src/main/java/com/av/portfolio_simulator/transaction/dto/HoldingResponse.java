package com.av.portfolio_simulator.transaction.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class HoldingResponse {


    private Long id;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal averageBuyPrice;

    // Current market price fetched from Alpha Vantage
    private BigDecimal currentPrice;

    // quantity * currentPrice
    private BigDecimal currentValue;

    // quantity * (currentPrice - averageBuyPrice)
    private BigDecimal unrealizedPnL;

    // ((currentPrice - averageBuyPrice) / averageBuyPrice) * 100
    private BigDecimal unrealizedPnLPercent;

}
