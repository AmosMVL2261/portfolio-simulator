package com.av.portfolio_simulator.competition.repository;

import com.av.portfolio_simulator.competition.entity.CompetitionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CompetitionParticipant persistence operations.
 */
@Repository
public interface CompetitionParticipantRepository extends JpaRepository<CompetitionParticipant, Long> {

    // Used to build the leaderboard
    List<CompetitionParticipant> findByCompetitionId(Long competitionId);

    // Used to prevent duplicate participation
    boolean existsByCompetitionIdAndUserId(Long competitionId, Long userId);

    // Used to verify a user is a participant before allowing trades
    Optional<CompetitionParticipant> findByCompetitionIdAndUserId(Long competitionId, Long userId);

}
