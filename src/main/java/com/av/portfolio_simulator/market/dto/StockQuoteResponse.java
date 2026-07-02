package com.av.portfolio_simulator.market.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Response returned to the client when querying the current price of a symbol.
 * Wraps the relevant fields from Alpha Vantage's Global Quote response.
 */
@Data
@Builder
public class StockQuoteResponse {

    private String symbol;
    private BigDecimal price;
    private BigDecimal change;
    private BigDecimal changePercent;
    private Long volume;
    private String latestTradingDay;

}
