package com.av.portfolio_simulator.auth.service;

import com.av.portfolio_simulator.auth.dto.AuthResponse;
import com.av.portfolio_simulator.auth.dto.LoginRequest;
import com.av.portfolio_simulator.auth.dto.RegisterRequest;
import com.av.portfolio_simulator.config.JwtService;
import com.av.portfolio_simulator.security.UserPrincipal;
import com.av.portfolio_simulator.user.entity.Role;
import com.av.portfolio_simulator.user.entity.User;
import com.av.portfolio_simulator.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service layer for authentication operations.
 * Handles user registration and login, delegating token generation
 * to JwtService and credential validation to Spring Security's AuthenticationManager.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user with the USER role.
     * Validates that the email and username are not already taken,
     * hashes the password, persists the user, and returns a JWT token.
     *
     * @throws IllegalArgumentException if the email or username is already registered
     */
    public AuthResponse register(RegisterRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        if(userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // Wrap the saved user in UserPrincipal to generate the token
        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal);

        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole().name());
    }

    /**
     * Authenticates an existing user using their email and password.
     * Delegates credential validation to Spring Security's AuthenticationManager,
     * which internally calls UserDetailsServiceImpl and BCrypt comparison.
     * Returns a JWT token on success.
     *
     * @throws org.springframework.security.core.AuthenticationException if credentials are invalid
     */
    public AuthResponse login(LoginRequest request) {
        // This single line triggers the full Spring Security authentication flow:
        // 1. Calls UserDetailsServiceImpl.loadUserByUsername(email)
        // 2. Compares the provided password with the stored BCrypt hash
        // 3. Throws AuthenticationException if either check fails
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Safely extract the principal — throw if the type is unexpected
        if (!(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new IllegalStateException("Unexpected principal type after authentication");
        }

        String token = jwtService.generateToken(principal);
        User user = principal.getUser();

        return new AuthResponse(
                token,
                user.getUsername(),
                principal.getEmail(),
                user.getRole().name()
        );
    }

}
