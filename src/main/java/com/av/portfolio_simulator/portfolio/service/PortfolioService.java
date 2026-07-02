package com.av.portfolio_simulator.portfolio.service;

import com.av.portfolio_simulator.portfolio.dto.CreatePortfolioRequest;
import com.av.portfolio_simulator.portfolio.dto.PortfolioResponse;
import com.av.portfolio_simulator.portfolio.entity.SimulatedPortfolio;
import com.av.portfolio_simulator.portfolio.repository.SimulatedPortfolioRepository;
import com.av.portfolio_simulator.security.UserPrincipal;
import com.av.portfolio_simulator.user.entity.User;
import com.av.portfolio_simulator.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service layer for simulated portfolio operations.
 * All operations are scoped to the authenticated user — a user
 * can only create, view, or modify their own portfolios.
 */
@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final SimulatedPortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new simulated portfolio for the authenticated user.
     * The cash balance starts equal to the initial capital.
     *
     * @throws IllegalArgumentException if the user already has a portfolio with the same name
     */
    @Transactional
    public PortfolioResponse create(CreatePortfolioRequest request, UserPrincipal principal) {
        if (portfolioRepository.existsByUserIdAndName(principal.getId(), request.getName())) {
            throw new IllegalArgumentException("You already have a portfolio named '" + request.getName() + "'");
        }

        User user = userRepository.findById(principal.getId()).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );

        SimulatedPortfolio portfolio = SimulatedPortfolio.builder()
                .user(user)
                .name(request.getName())
                .initialCapital(request.getInitialCapital())
                .cashBalance(request.getInitialCapital())
                .build();

        SimulatedPortfolio saved = portfolioRepository.save(portfolio);
        return toResponse(saved, BigDecimal.ZERO);

    }

    /**
     * Returns all portfolios belonging to the authenticated user.
     * Holdings value is set to 0 until the holdings module is implemented.
     */
    @Transactional(readOnly = true)
    public List<PortfolioResponse> getAll(UserPrincipal principal) {
        return portfolioRepository.findByUserId(principal.getId())
                .stream()
                .map(p -> toResponse(p, BigDecimal.ZERO))
                .toList();
    }

    /**
     * Returns a specific portfolio by ID, scoped to the authenticated user.
     *
     * @throws IllegalArgumentException if the portfolio does not exist or belongs to another user
     */
    @Transactional(readOnly = true)
    public PortfolioResponse getById(Long portfolioId, UserPrincipal principal) {
        SimulatedPortfolio portfolio = portfolioRepository.findByIdAndUserId(portfolioId, principal.getId()).orElseThrow(
                () -> new IllegalArgumentException("Portfolio not found")
        );
        return toResponse(portfolio, BigDecimal.ZERO);
    }

    /**
     * Maps a SimulatedPortfolio entity to a PortfolioResponse DTO.
     * Computes total value, total return, and return percentage.
     *
     * @param portfolio     the portfolio entity
     * @param holdingsValue the current market value of all holdings (0 until holdings module is built)
     */
    private PortfolioResponse toResponse(SimulatedPortfolio portfolio, BigDecimal holdingsValue) {
        BigDecimal holdingsValueScaled = holdingsValue.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalValue = portfolio.getCashBalance().add(holdingsValueScaled);
        BigDecimal totalReturn = totalValue.subtract(portfolio.getInitialCapital());
        BigDecimal totalReturnPercent = portfolio.getInitialCapital().compareTo(BigDecimal.ZERO) == 0 ?
                BigDecimal.ZERO : totalReturn
                    .divide(portfolio.getInitialCapital(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

        return PortfolioResponse.builder()
                .id(portfolio.getId())
                .name(portfolio.getName())
                .initialCapital(portfolio.getInitialCapital())
                .cashBalance(portfolio.getCashBalance())
                .holdingsValue(holdingsValueScaled)
                .totalValue(totalValue)
                .totalReturn(totalReturn)
                .totalReturnPercent(totalReturnPercent)
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }

}
