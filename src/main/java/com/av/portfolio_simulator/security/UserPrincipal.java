package com.av.portfolio_simulator.security;

import com.av.portfolio_simulator.user.entity.User;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security adapter for the User entity.
 * Implements UserDetails to provide authentication and authorization information
 * without coupling the User entity to Spring Security concerns.
 *
 * Wraps a User instance and delegates all security-related calls to it.
 */
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final User user;

    /**
     * Exposes the underlying User entity for cases where full user
     * details are needed after authentication (e.g. building responses).
     */
    public User getUser() {
        return user;
    }

    /**
     * Maps the user's Role enum to a Spring Security GrantedAuthority.
     * The "ROLE_" prefix is required by Spring Security's hasRole() checks.
     */
    @Override
    @Nonnull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+this.user.getRole()));
    }

    /**
     * Exposes the user's database ID for use in service layer operations
     * where the authenticated user's ID is needed.
     */
    public Long getId() {
        return user.getId();
    }

    /**
     * Exposes the user's email for convenience when building responses.
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * Returns the email as the Spring Security username.
     * Email is preferred over username because it is guaranteed to be unique
     * and is what the user provides at login.
     */
    @Override
    @Nonnull
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    // Account status checks delegate to the User entity.
    // All return true except isEnabled(), which reflects the actual user state.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

}
