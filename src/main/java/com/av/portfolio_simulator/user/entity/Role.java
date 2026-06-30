package com.av.portfolio_simulator.user.entity;

/**
 * Defines the available roles in the system.
 * Used by Spring Security to enforce access control via @PreAuthorize.
 *
 * ADMIN - Full access to all endpoints including user management.
 * USER  - Standard access to portfolio and competition features.
 */
public enum Role {
    ADMIN,
    USER
}
