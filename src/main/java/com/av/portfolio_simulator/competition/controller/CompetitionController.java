package com.av.portfolio_simulator.competition.controller;

import com.av.portfolio_simulator.common.response.ApiResponse;
import com.av.portfolio_simulator.competition.dto.CompetitionResponse;
import com.av.portfolio_simulator.competition.dto.CreateCompetitionRequest;
import com.av.portfolio_simulator.competition.dto.LeaderboardEntry;
import com.av.portfolio_simulator.competition.entity.CompetitionStatus;
import com.av.portfolio_simulator.competition.service.CompetitionService;
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
 * REST controller for competition management.
 */
@RestController
@RequestMapping("/competitions")
@RequiredArgsConstructor
@Tag(name = "Competitions", description = "Create and join trading competitions")
@SecurityRequirement(name = "bearerAuth")
public class CompetitionController {

    private final CompetitionService competitionService;

    /**
     * Creates a new competition. Creator must join separately to participate.
     */
    @PostMapping
    @Operation(summary = "Create a new competition")
    public ResponseEntity<ApiResponse<CompetitionResponse>> create(
        @Valid @RequestBody CreateCompetitionRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        CompetitionResponse response = competitionService.create(request, principal);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Competition created successfully", response));
    }

    /**
     * Returns all competitions filtered by status.
     * Defaults to ACTIVE if no status is provided.
     */
    @GetMapping
    @Operation(summary = "List competitions by status")
    public ResponseEntity<ApiResponse<List<CompetitionResponse>>> getByStatus(
        @RequestParam(defaultValue = "ACTIVE") CompetitionStatus status
    ) {
        List<CompetitionResponse> competitions = competitionService.getByStatus(status);
        return ResponseEntity.ok(ApiResponse.ok("Competitions retrieved successfully", competitions));
    }

    /**
     * Joins a competition and creates a dedicated portfolio automatically.
     */
    @PostMapping("/{competitionId}/join")
    @Operation(summary = "Join a competition")
    public ResponseEntity<ApiResponse<CompetitionResponse>> join(
        @PathVariable Long competitionId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        CompetitionResponse response = competitionService.join(competitionId, principal);
        return ResponseEntity.ok(ApiResponse.ok("Successfully joined the competition", response));
    }

    /**
     * Returns the live leaderboard ranked by total return percentage.
     */
    @GetMapping("/{competitionId}/leaderboard")
    @Operation(summary = "Get competition leaderboard")
    public ResponseEntity<ApiResponse<List<LeaderboardEntry>>> getLeaderboard(
        @PathVariable Long competitionId
    ) {
        List<LeaderboardEntry> leaderboard = competitionService.getLeaderboard(competitionId);
        return ResponseEntity.ok(ApiResponse.ok("Leaderboard retrieved successfully", leaderboard));
    }

}
