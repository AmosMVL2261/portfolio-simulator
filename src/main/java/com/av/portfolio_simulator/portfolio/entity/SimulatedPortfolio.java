package com.av.portfolio_simulator.portfolio.entity;


import com.av.portfolio_simulator.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a simulated investment portfolio belonging to a user.
 * Tracks available cash balance separately from invested holdings.
 * A user may own multiple portfolios with unique names.
 */
@Entity
@Table(name = "simulated_portfolios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulatedPortfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who owns this portfolio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Display name for the portfolio, unique per user
    @Column(nullable = false, length = 100)
    private String name;

    // The starting capital assigned when the portfolio was created
    @Column(name = "initial_capital", nullable = false, precision = 19, scale = 2)
    private BigDecimal initialCapital;

    // Available cash for new purchases (decreases on buy, increases on sell)
    @Column(name = "cash_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal cashBalance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
