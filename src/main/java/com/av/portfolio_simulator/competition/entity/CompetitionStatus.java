package com.av.portfolio_simulator.competition.entity;

/**
 * Lifecycle states of a competition.
 *
 * PENDING  — created but start_date has not been reached yet
 * ACTIVE   — start_date has passed, participants can trade
 * FINISHED — end_date has passed, leaderboard is final
 */
public enum CompetitionStatus {
    PENDING,
    ACTIVE,
    FINISHED
}
