package com.av.portfolio_simulator.market.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a single result from a symbol search query.
 * Used to help users find the correct ticker symbol before buying.
 */
@Data
@Builder
public class StockSearchResult {

    private String symbol;
    private String name;
    private String type;
    private String region;
    private String currency;

}
