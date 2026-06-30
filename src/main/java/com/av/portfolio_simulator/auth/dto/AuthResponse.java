package com.av.portfolio_simulator.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response body returned after successful registration or login.
 * Contains the JWT token and basic user information to avoid
 * an additional profile request from the client.
 */
@Data
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String username;
    private String email;
    private String role;

}
