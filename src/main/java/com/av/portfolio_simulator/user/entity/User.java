package com.av.portfolio_simulator.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a registered user in the system.
 * Strictly responsible for database persistence only.
 * Authentication concerns are handled separately by UserPrincipal.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Must be unique across all users, used for display purposes
    @Column(nullable = false, unique = true, length = 80)
    private String username;

    // Used as the authentication identifier (login credential)
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // Stored as a BCrypt hash, never as plain text
    @Column(nullable = false)
    private String password;

    // Stored as a string in the database (e.g. "ADMIN", "USER")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Allows disabling a user account without deleting it
    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    // Set automatically on insert, never updated afterwards
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Automatically sets the creation timestamp before the entity is persisted.
     * The updatable = false constraint on the column ensures it is never overwritten.
     */
    @PrePersist
    protected  void onCreate(){
        this.createdAt = LocalDateTime.now();
    }

}
