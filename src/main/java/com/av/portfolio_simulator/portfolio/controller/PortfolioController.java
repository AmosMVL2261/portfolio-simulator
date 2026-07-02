package com.av.portfolio_simulator.portfolio.controller;


import com.av.portfolio_simulator.common.response.ApiResponse;
import com.av.portfolio_simulator.portfolio.dto.CreatePortfolioRequest;
import com.av.portfolio_simulator.portfolio.dto.PortfolioResponse;
import com.av.portfolio_simulator.portfolio.service.PortfolioService;
import com.av.portfolio_simulator.security.UserPrincipal;
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
 * REST controller for simulated portfolio operations.
 * All endpoints require a valid JWT token.
 */
@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
@Tag(name = "Portfolios", description = "Simulated portfolio management")
@SecurityRequirement(name = "bearerAuth")
public class PortfolioController {

    private final PortfolioService portfolioService;

    /**
     * Creates a new simulated portfolio for the authenticated user.
     */
    @PostMapping
    @Operation(summary = "Create a new simulated portfolio")
    public ResponseEntity<ApiResponse<PortfolioResponse>> create(
        @Valid @RequestBody CreatePortfolioRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        PortfolioResponse response = portfolioService.create(request, principal);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Portfolio created successfully", response));
    }

    /**
     * Returns all portfolios belonging to the authenticated user.
     */
    @GetMapping
    @Operation(summary = "Get all portfolios for the authenticated user")
    public ResponseEntity<ApiResponse<List<PortfolioResponse>>> getAll(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<PortfolioResponse> portfolios = portfolioService.getAll(principal);
        return ResponseEntity.ok(ApiResponse.ok("Portfolios retrieved successfully", portfolios));
    }

    /**
     * Returns a specific portfolio by ID, scoped to the authenticated user.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a portfolio by ID")
    public ResponseEntity<ApiResponse<PortfolioResponse>> getById(
        @PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        PortfolioResponse response = portfolioService.getById(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Portfolio retrieved successfully", response));
    }

}
