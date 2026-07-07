package com.av.portfolio_simulator.competition.repository;

import com.av.portfolio_simulator.competition.entity.Competition;
import com.av.portfolio_simulator.competition.entity.CompetitionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Competition persistence operations.
 */
@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {

    // Used to list open competitions a user can join
    List<Competition> findByStatus(CompetitionStatus status);

}
