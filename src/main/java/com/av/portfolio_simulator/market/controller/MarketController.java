package com.av.portfolio_simulator.market.controller;

import com.av.portfolio_simulator.common.response.ApiResponse;
import com.av.portfolio_simulator.market.dto.StockQuoteResponse;
import com.av.portfolio_simulator.market.dto.StockSearchResult;
import com.av.portfolio_simulator.market.service.MarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing market data endpoints.
 * Delegates to MarketService which handles caching and Alpha Vantage communication.
 */
@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
@Tag(name = "Market", description = "Real-time stock and EFT market data")
@SecurityRequirement(name = "bearerAuth")
public class MarketController {

    private final MarketService marketService;

    /**
     * Searches for stocks and ETFs by company name or ticker symbol.
     * Example: GET /market/search?q=Apple
     */
    @GetMapping("/search")
    @Operation(summary = "Search for stocks and EFTs by keyword")
    public ResponseEntity<ApiResponse<List<StockSearchResult>>> search(@RequestParam String q) {
        List<StockSearchResult> results = marketService.searchSymbol(q);
        return ResponseEntity.ok(
                ApiResponse.ok("Search completed", results)
        );
    }

    /**
     * Returns the current market price for a given ticker symbol.
     * Example: GET /market/price/AAPL
     */
    @GetMapping("/price/{symbol}")
    @Operation(summary = "Get current market price for a symbol")
    public ResponseEntity<ApiResponse<StockQuoteResponse>> getPrice(@PathVariable String symbol) {
        StockQuoteResponse quoteResponse = marketService.getQuote(symbol);
        return ResponseEntity.ok(
            ApiResponse.ok("Quote retrieved successfully", quoteResponse)
        );
    }

}
