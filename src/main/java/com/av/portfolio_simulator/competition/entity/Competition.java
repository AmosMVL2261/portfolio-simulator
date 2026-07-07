package com.av.portfolio_simulator.competition.entity;

import com.av.portfolio_simulator.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A competition where multiple users trade with equal starting capital
 * over a defined period. The winner is determined by highest total return.
 */
@Entity
@Table(name = "competitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    // All participants start with exact capital
    @Column(name = "initial_capital", nullable = false, precision = 19, scale = 2)
    private BigDecimal initialCapital;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CompetitionStatus status;

    // The user who created the competition
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        // Status defaults to PENDING until start_date is reached
        if(this.status == null) {
            this.status = CompetitionStatus.PENDING;
        }
    }

}
