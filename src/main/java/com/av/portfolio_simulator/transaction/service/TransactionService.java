package com.av.portfolio_simulator.transaction.service;

import com.av.portfolio_simulator.market.service.MarketService;
import com.av.portfolio_simulator.portfolio.entity.SimulatedPortfolio;
import com.av.portfolio_simulator.portfolio.repository.SimulatedPortfolioRepository;
import com.av.portfolio_simulator.security.UserPrincipal;
import com.av.portfolio_simulator.transaction.dto.BuyRequest;
import com.av.portfolio_simulator.transaction.dto.HoldingResponse;
import com.av.portfolio_simulator.transaction.dto.SellRequest;
import com.av.portfolio_simulator.transaction.dto.TransactionResponse;
import com.av.portfolio_simulator.transaction.entity.Holding;
import com.av.portfolio_simulator.transaction.entity.Transaction;
import com.av.portfolio_simulator.transaction.entity.TransactionType;
import com.av.portfolio_simulator.transaction.repository.HoldingRepository;
import com.av.portfolio_simulator.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service layer for buy/sell transactions and holdings management.
 *
 * Buy logic:
 *   1. Fetch current market price from Alpha Vantage
 *   2. Validate portfolio belongs to user and has sufficient cash
 *   3. Deduct total cost from cash balance
 *   4. Create or update the holding using weighted average price
 *   5. Record the transaction
 *
 * Sell logic:
 *   1. Fetch current market price from Alpha Vantage
 *   2. Validate portfolio belongs to user and has sufficient shares
 *   3. Add proceeds to cash balance
 *   4. Reduce holding quantity (delete if fully sold)
 *   5. Record the transaction
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final SimulatedPortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final TransactionRepository transactionRepository;
    private final MarketService marketService;

    /**
     * Executes a simulated buy order at the current market price.
     *
     * @throws IllegalArgumentException if portfolio not found, insufficient cash,
     *                                  or symbol not found in the market
     */
    @Transactional
    public TransactionResponse buy(Long portfolioId, BuyRequest request, UserPrincipal userPrincipal) {
        SimulatedPortfolio portfolio = getPortfolioForUser(portfolioId, userPrincipal);

        // Fetch current market price - throws if symbol not found
        BigDecimal price = marketService.getQuote(request.getSymbol().toUpperCase()).getPrice();
        BigDecimal totalCost = price.multiply(request.getQuantity()).setScale(2, RoundingMode.HALF_UP);

        // Validate sufficient cash balance
        if(portfolio.getCashBalance().compareTo(totalCost) < 0) {
            throw new IllegalArgumentException(
                String.format(
                    "Insufficient cash balance. Required: %.2f, Available: %.2f",
                    totalCost,
                    portfolio.getCashBalance()
                )
            );
        }

        // Deduct cost from cash balance
        portfolio.setCashBalance(portfolio.getCashBalance().subtract(totalCost));
        portfolioRepository.save(portfolio);

        // Update or create holding using weighted average price
        String symbol = request.getSymbol().toUpperCase();
        Holding holding = holdingRepository
                                        .findByPortfolioIdAndSymbol(portfolioId, symbol)
                                        .orElse(null);
        if(holding == null) {
            // First purchase of this symbol - create new holding
            holding = Holding.builder()
                            .portfolio(portfolio)
                            .symbol(symbol)
                            .quantity(request.getQuantity())
                            .averageBuyPrice(price)
                            .build();
        } else {
            // Subsequent purchase - recalculate weighted average price
            // Formula: (existingQty * existingAvg + newQty * newPrice) / totalQty
            BigDecimal existingValue = holding.getQuantity().multiply(holding.getAverageBuyPrice());
            BigDecimal newValue = request.getQuantity().multiply(price);
            BigDecimal totalQuantity = holding.getQuantity().add(request.getQuantity());
            BigDecimal newAverage = existingValue.add(newValue).divide(totalQuantity, 2, RoundingMode.HALF_UP);
            holding.setQuantity(totalQuantity);
            holding.setAverageBuyPrice(newAverage);
        }
        holdingRepository.save(holding);
        // Record immutable transaction
        Transaction transaction = Transaction.builder()
                .portfolio(portfolio)
                .symbol(symbol)
                .type(TransactionType.BUY)
                .quantity(request.getQuantity())
                .price(price)
                .totalAmount(totalCost)
                .build();
        transactionRepository.save(transaction);
        return toTransactionResponse(transaction);
    }

    /**
     * Executes a simulated sell order at the current market price.
     *
     * @throws IllegalArgumentException if portfolio not found, no holding exists
     *                                  for the symbol, or insufficient shares
     */
    @Transactional
    public TransactionResponse sell(Long portfolioId, SellRequest request, UserPrincipal userPrincipal) {
        SimulatedPortfolio portfolio = getPortfolioForUser(portfolioId, userPrincipal);

        String symbol = request.getSymbol().toUpperCase();

        // Validate holding exists
        Holding holding = holdingRepository.findByPortfolioIdAndSymbol(portfolioId, symbol).orElseThrow(
            () -> new IllegalArgumentException(
                "You don't own any shares of " + symbol + " in this portfolio"
            )
        );

        // Validate sufficient shares
        if(holding.getQuantity().compareTo(request.getQuantity()) < 0) {
            throw new IllegalArgumentException(
                String.format(
                    "Insufficient shares. Requested: %s, Available: %s",
                    request.getQuantity().toPlainString(),
                    holding.getQuantity().toPlainString()
                )
            );
        }

        // Fetch current market price
        BigDecimal price =marketService.getQuote(symbol).getPrice();
        BigDecimal proceeds = price.multiply(request.getQuantity()).setScale(2, RoundingMode.HALF_UP);

        // Add proceeds to cash balance
        portfolio.setCashBalance(portfolio.getCashBalance().add(proceeds));
        portfolioRepository.save(portfolio);

        // Reduce or delete holding
        BigDecimal remainingQuantity = holding.getQuantity().subtract(request.getQuantity());

        if(remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
            // All shares sold - remove thw holding entirely
            holdingRepository.delete(holding);
        } else {
            holding.setQuantity(remainingQuantity);
            holdingRepository.save(holding);
        }

        // Record immutable transaction
        Transaction transaction = Transaction.builder()
                .portfolio(portfolio)
                .symbol(symbol)
                .type(TransactionType.SELL)
                .quantity(request.getQuantity())
                .price(price)
                .totalAmount(proceeds)
                .build();
        transactionRepository.save(transaction);

        return toTransactionResponse(transaction);
    }

    /**
     * Returns all current holdings for a portfolio with live market prices
     * and computed unrealized profit/loss for each position.
     */
    @Transactional(readOnly = true)
    public List<HoldingResponse> getHoldings(Long portfolioId, UserPrincipal userPrincipal) {

        getPortfolioForUser(portfolioId, userPrincipal);

        return holdingRepository.findByPortfolioId(portfolioId).stream()
            .map(holding -> {
                BigDecimal currentPrice = marketService.getQuote(holding.getSymbol()).getPrice();
                return toHoldingResponse(holding, currentPrice);
            })
            .toList();
    }

    /**
     * Returns the full transaction history for a portfolio, newest first.
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(Long portfolioId, UserPrincipal userPrincipal) {

        getPortfolioForUser(portfolioId, userPrincipal);

        return transactionRepository.findByPortfolioIdOrderByExecutedAtDesc(portfolioId)
            .stream()
            .map(this::toTransactionResponse)
            .toList();
    }

    /**
     * Validates that the portfolio exists and belongs to the authenticated user.
     *
     * @throws IllegalArgumentException if not found or owned by another user
     */
    private SimulatedPortfolio getPortfolioForUser(Long portfolioId, UserPrincipal userPrincipal) {
        return portfolioRepository.findByIdAndUserId(portfolioId, userPrincipal.getId()).orElseThrow(
                () -> new IllegalArgumentException("Portfolio not found")
        );
    }

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .symbol(transaction.getSymbol())
                .type(transaction.getType())
                .quantity(transaction.getQuantity())
                .price(transaction.getPrice())
                .totalAmount(transaction.getTotalAmount())
                .executedAt(transaction.getExecutedAt())
                .build();
    }

    private HoldingResponse toHoldingResponse(Holding holding, BigDecimal currentPrice) {
        BigDecimal currentValue = currentPrice
                .multiply(holding.getQuantity())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal unrealizedPnL = currentPrice
                .subtract(holding.getAverageBuyPrice())
                .multiply(holding.getQuantity())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal unrealizedPnLPercent = holding.getAverageBuyPrice().compareTo(BigDecimal.ZERO) == 0
            ?
                BigDecimal.ZERO
            :
                currentPrice.subtract(holding.getAverageBuyPrice())
                .divide(holding.getAverageBuyPrice(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        return HoldingResponse.builder()
                .id(holding.getId())
                .symbol(holding.getSymbol())
                .quantity(holding.getQuantity())
                .averageBuyPrice(holding.getAverageBuyPrice())
                .currentPrice(currentPrice)
                .currentValue(currentValue)
                .unrealizedPnL(unrealizedPnL)
                .unrealizedPnLPercent(unrealizedPnLPercent)
                .build();
    }

}
