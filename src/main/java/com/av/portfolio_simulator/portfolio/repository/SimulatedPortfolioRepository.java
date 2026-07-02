package com.av.portfolio_simulator.portfolio.repository;

import com.av.portfolio_simulator.portfolio.entity.SimulatedPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SimulatedPortfolio persistence operations.
 */
@Repository
public interface SimulatedPortfolioRepository extends JpaRepository<SimulatedPortfolio, Long> {

    // Retrieves all portfolios owned by a specific user
    List<SimulatedPortfolio> findByUserId(Long userId);

    // Used to find a specific portfolio by ID, ensuring it belongs to the requesting user
    Optional<SimulatedPortfolio> findByIdAndUserId(Long id, Long userId);

    // Used during creation to prevent duplicate portfolio names per user
    boolean existsByUserIdAndName(Long userId, String name);

}
