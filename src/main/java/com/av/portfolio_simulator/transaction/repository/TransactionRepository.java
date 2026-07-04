package com.av.portfolio_simulator.transaction.repository;

import com.av.portfolio_simulator.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Transaction persistence operations.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Retrieves the full transaction history for a portfolio, newest first
    List<Transaction> findByPortfolioIdOrderByExecutedAtDesc(Long portfolioId);

}
