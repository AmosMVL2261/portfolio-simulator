package com.av.portfolio_simulator.transaction.entity;

import com.av.portfolio_simulator.portfolio.entity.SimulatedPortfolio;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents the current position of a portfolio in a specific stock.
 * There is at most one Holding per symbol per portfolio.
 *
 * The average buy price is recalculated on every purchase using the
 * weighted average formula:
 *   newAvg = (existingQty * existingAvg + newQty * newPrice) / (existingQty + newQty)
 *
 * When shares are sold, quantity decreases but average price stays the same.
 * When all shares are sold, the holding is deleted.
 */
@Entity
@Table(name = "holdings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private SimulatedPortfolio portfolio;

    @Column(nullable = false, length = 20)
    private String symbol;

    // Current number of shares held
    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity;

    // Weighted average price paid per share across all purchases
    @Column(name = "average_buy_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal averageBuyPrice;

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
