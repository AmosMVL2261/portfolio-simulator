package com.av.portfolio_simulator.config;

import com.av.portfolio_simulator.user.service.UserDetailsServiceImpl;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that intercepts every incoming HTTP request to validate JWT tokens.
 * Extends OncePerRequestFilter to guarantee single execution per request.
 * If a valid token is found, the user is authenticated in the SecurityContext.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        // If the Authorization header is missing or doesn't start with "Bearer ",
        // skip this filter and continue the chain (request will be handled as unauthenticated)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the token by removing the "Bearer " prefix (7 characters)
        final String jwt = authHeader.substring(7);
        final String email = jwtService.extractEmail(jwt);

        // Only proceed if we got an email and the user isn't already authenticated
        // (avoids redundant processing on the same request)
        if(email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if(jwtService.isTokenValid(jwt, userDetails)) {
                // Build an authentication token with the user's authorities
                // Credentials are null because we don't need the password at this point
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // Attach request details (IP address, session ID) to the authentication token
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Register the authenticated user in the SecurityContext
                // From this point on, the request is considered authenticated
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // Continue the filter chain regardless of authentication result
        filterChain.doFilter(request, response);
    }

}
