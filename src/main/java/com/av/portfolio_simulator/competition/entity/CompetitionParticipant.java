package com.av.portfolio_simulator.competition.entity;

import com.av.portfolio_simulator.portfolio.entity.SimulatedPortfolio;
import com.av.portfolio_simulator.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Links a user to a competition and their dedicated portfolio for that competition.
 * Each user can only join a competition once.
 * A new portfolio is automatically created when the user joins.
 */
@Entity
@Table(name = "competition_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Dedicated portfolio created automatically when joining
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private SimulatedPortfolio portfolio;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreated() {
        this.joinedAt = LocalDateTime.now();
    }

}
