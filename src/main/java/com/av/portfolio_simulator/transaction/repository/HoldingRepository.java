package com.av.portfolio_simulator.transaction.repository;

import com.av.portfolio_simulator.transaction.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Holding persistence operations.
 */
@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    // Retrieves all current positions in a portfolio
    List<Holding> findByPortfolioId(Long portfolioId);

    // Used during buy/sell to find an existing position for a symbol
    Optional<Holding> findByPortfolioIdAndSymbol(Long portfolioId, String symbol);

}
