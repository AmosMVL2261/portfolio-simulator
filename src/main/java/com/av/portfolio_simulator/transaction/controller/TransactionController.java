package com.av.portfolio_simulator.transaction.controller;

import com.av.portfolio_simulator.common.response.ApiResponse;
import com.av.portfolio_simulator.security.UserPrincipal;
import com.av.portfolio_simulator.transaction.dto.BuyRequest;
import com.av.portfolio_simulator.transaction.dto.HoldingResponse;
import com.av.portfolio_simulator.transaction.dto.SellRequest;
import com.av.portfolio_simulator.transaction.dto.TransactionResponse;
import com.av.portfolio_simulator.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for buy/sell transactions and holdings.
 * All endpoints are scoped to a specific portfolio owned by the authenticated user.
 */
@RestController
@RequestMapping("/portfolios/{portfolioId}")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Buy/sell operations and holdings")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Buys shares of a stock at the current market price.
     * Deducts the total cost from the portfolio's cash balance.
     */
    @PostMapping("/buy")
    @Operation(summary = "Buy shares at current market price")
    public ResponseEntity<ApiResponse<TransactionResponse>> buy(
        @PathVariable Long portfolioId,
        @Valid @RequestBody BuyRequest request,
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        TransactionResponse response = transactionService.buy(portfolioId, request, userPrincipal);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok("Buy order executed successfully", response)
        );
    }

    /**
     * Sells shares of a stock at the current market price.
     * Adds the proceeds to the portfolio's cash balance.
     */
    @PostMapping("/sell")
    @Operation(summary = "Sell shares at current market price")
    public ResponseEntity<ApiResponse<TransactionResponse>> sell(
        @PathVariable Long portfolioId,
        @Valid @RequestBody SellRequest request,
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        TransactionResponse response = transactionService.sell(portfolioId, request, userPrincipal);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok("Sell order executed successfully", response)
        );
    }

    /**
     * Returns all current positions in the portfolio with live prices
     * and unrealized profit/loss for each holding.
     */
    @GetMapping("/holdings")
    @Operation(summary = "Get all current holdings with live prices")
    public ResponseEntity<ApiResponse<List<HoldingResponse>>> getHoldings(
        @PathVariable Long portfolioId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<HoldingResponse> holdings = transactionService.getHoldings(portfolioId, principal);
        return ResponseEntity.ok(ApiResponse.ok("Holdings retrieved successfully", holdings));
    }

    /**
     * Returns the complete transaction history for the portfolio, newest first.
     */
    @GetMapping("/transactions")
    @Operation(summary = "Get full transaction history")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(
        @PathVariable Long portfolioId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<TransactionResponse> transactions = transactionService.getTransactions(portfolioId, principal);
        return ResponseEntity.ok(ApiResponse.ok("Transactions retrieved successfully", transactions));
    }

}
