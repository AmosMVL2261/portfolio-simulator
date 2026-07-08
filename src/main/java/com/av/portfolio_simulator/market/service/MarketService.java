package com.av.portfolio_simulator.market.service;

import com.av.portfolio_simulator.common.exception.MarketDataException;
import com.av.portfolio_simulator.common.exception.ResourceNotFoundException;
import com.av.portfolio_simulator.market.dto.StockQuoteResponse;
import com.av.portfolio_simulator.market.dto.StockSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service for interacting with the Alpha Vantage market data API.
 *
 * Results are cached to minimize API calls against the free tier limit (25/day).
 * Cache duration is controlled by Spring's simple cache — entries persist
 * until the application restarts. A more robust solution (Redis, Caffeine)
 * can be added later without changing this service.
 */
@Slf4j
@Service
@SuppressWarnings({"unchecked", "LoggingSimilarMessage"})
@RequiredArgsConstructor
public class MarketService {

    @Value("${ALPHAVANTAGE_BASE_URL}")
    private String baseUrl;

    @Value("${ALPHAVANTAGE_API_KEY}")
    private String apiKey;

    private final RestClient restClient;

    /**
     * Searches for stocks and ETFs matching the given keyword.
     * Useful for finding the correct ticker symbol before placing a simulated order.
     * Results are cached by keyword to avoid repeated API calls for the same query.
     *
     * @param keyword company name or partial ticker (e.g. "Apple", "AAPL", "SPY")
     * @return list of matching symbols with basic metadata
     */
    @Cacheable(value = "symbolSearch", key = "#keyword.toUpperCase()")
    public List<StockSearchResult> searchSymbol(String keyword) {
        log.info("Calling Alpha Vantage SYMBOL_SEARCH for keyword: {}", keyword);

        Map<String, Object> response = restClient.get()
                .uri(baseUrl+"?function=SYMBOL_SEARCH&keywords={keyword}&apikey={apikey}", keyword, apiKey)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response != null && response.containsKey("Information")) {
            log.warn("Alpha Vantage API limit reached: {}", response.get("Information"));
            throw new MarketDataException("Market data temporarily unavailable. API rate limit reached.");
        }

        if (response == null || !response.containsKey("bestMatches")) {
            return Collections.emptyList();
        }

        List<Map<String, String>> matches = (List<Map<String, String>>) response.get("bestMatches");

        return matches.stream()
                .map(match -> StockSearchResult.builder()
                        .symbol(match.get("1. symbol"))
                        .name(("null".equals(match.get("2. name"))) ? null : match.get("2. name"))
                        .type(match.get("3. type"))
                        .region(match.get("4. region"))
                        .currency(match.get("8. currency"))
                        .build()
                )
                .toList();
    }

    /**
     * Fetches the current market price for a given symbol.
     * Results are cached by symbol to avoid burning API calls on repeated price checks.
     * Cache entries expire on application restart — acceptable for a simulator.
     *
     * @param symbol ticker symbol (e.g. "AAPL", "SPY", "MSFT")
     * @return current quote with price, change, and volume
     * @throws ResourceNotFoundException if the symbol
     * @throws MarketDataException if is not found or the API returns no data
     */
    @Cacheable(value = "stockQuote", key = "#symbol.toUpperCase()", unless = "#result == null")
    public StockQuoteResponse getQuote(String symbol) {
        log.info("Calling Alpha Vantage GLOBAL_QUOTE for symbol: {}", symbol);

        Map<String, Object> response = restClient.get()
                .uri(baseUrl + "?function=GLOBAL_QUOTE&symbol={symbol}&apikey={apikey}", symbol, apiKey)
                .retrieve()
                .body(Map.class);

        log.info("Alpha Vantage raw response: {}", response);

        // Detect rate limit or API key errors
        if (response != null && response.containsKey("Information")) {
            log.warn("Alpha Vantage API limit reached: {}", response.get("Information"));
            throw new MarketDataException("Market data temporarily unavailable. API rate limit reached.");
        }

        if (response != null && response.containsKey("Note")) {
            log.warn("Alpha Vantage API note: {}", response.get("Note"));
            throw new MarketDataException("Market data temporarily unavailable. Please try again later.");
        }

        if (response == null || !response.containsKey("Global Quote")) {
            throw new ResourceNotFoundException("Symbol not found: " + symbol);
        }

        Map<String, String> quote = (Map<String, String>) response.get("Global Quote");

        if (quote == null || quote.isEmpty() || !quote.containsKey("05. price")) {
            throw new ResourceNotFoundException("Symbol not found or market data unavailable: " + symbol);
        }

        String rawChangePercent = quote.getOrDefault("10. change percent", "0%")
                .replace("%", "")
                .trim();

        return StockQuoteResponse.builder()
                .symbol(quote.get("01. symbol"))
                .price(new BigDecimal(quote.get("05. price")))
                .change(new BigDecimal(quote.getOrDefault("09. change", "0")))
                .changePercent(new BigDecimal(rawChangePercent))
                .volume(Long.parseLong(quote.getOrDefault("06. volume", "0")))
                .latestTradingDay(quote.get("07. latest trading day"))
                .build();
    }

}
