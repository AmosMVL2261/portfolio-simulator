package com.av.portfolio_simulator.transaction.entity;

import com.av.portfolio_simulator.portfolio.entity.SimulatedPortfolio;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Immutable record of a single buy or sell operation.
 * Transactions are never modified after creation — they form the
 * complete audit trail of all portfolio activity.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private SimulatedPortfolio portfolio;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity;

    // Market price at the moment the transaction was executed
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    // quantity * price — stored for convenience and audit purposes
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "executed_at", nullable = false, updatable = false)
    private LocalDateTime executedAt;

    @PrePersist
    protected void onCreate() {
        this.executedAt = LocalDateTime.now();
    }

}
