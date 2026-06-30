package com.av.portfolio_simulator.user.repository;

import com.av.portfolio_simulator.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for User entity database operations.
 * Extends JpaRepository to inherit standard CRUD methods.
 * Custom finders are used by the auth layer for login and duplicate validation.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // Used by UserDetailsService to load a user during authentication
    Optional<User> findByEmail(String email);

    // Used by UserDetailsService to load a user during authentication
    Optional<User> findByUsername(String username);

    // Used during registration to prevent duplicate emails
    boolean existsByEmail(String email);

    // Used during registration to prevent duplicate usernames
    boolean existsByUsername(String username);

}
