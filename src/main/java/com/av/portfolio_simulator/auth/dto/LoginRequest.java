package com.av.portfolio_simulator.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for the POST /auth/login endpoint.
 * Uses email as the authentication identifier, consistent with UserPrincipal.
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

}
