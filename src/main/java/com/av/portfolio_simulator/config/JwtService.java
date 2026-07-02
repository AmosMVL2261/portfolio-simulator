package com.av.portfolio_simulator.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Service responsible for JWT token generation and validation.
 * Handles all cryptographic operations related to authentication tokens.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    // Secret key used to sign tokens, injected from application.properties
    @Value("${jwt.secret}")
    private String secret;

    // Token validity duration in milliseconds (default: 24 hours)
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Generates a signed JWT token for the given user.
     * Uses the user's email (getUsername()) as the token subject.
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the email (subject) from a JWT token.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validates a token by checking that it belongs to the given user
     * and has not expired.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Checks whether a token's expiration date is in the past.
     */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Generic method to extract any claim from a token using a resolver function.
     * Parses and verifies the token signature before extracting the payload.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    /**
     * Builds the HMAC-SHA signing key from the secret string.
     * The secret must be at least 32 characters long for HS256.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

}
