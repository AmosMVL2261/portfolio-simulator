package com.av.portfolio_simulator.competition.service;

import com.av.portfolio_simulator.common.exception.BusinessException;
import com.av.portfolio_simulator.common.exception.ResourceNotFoundException;
import com.av.portfolio_simulator.competition.dto.CompetitionResponse;
import com.av.portfolio_simulator.competition.dto.CreateCompetitionRequest;
import com.av.portfolio_simulator.competition.dto.LeaderboardEntry;
import com.av.portfolio_simulator.competition.entity.Competition;
import com.av.portfolio_simulator.competition.entity.CompetitionParticipant;
import com.av.portfolio_simulator.competition.entity.CompetitionStatus;
import com.av.portfolio_simulator.competition.repository.CompetitionParticipantRepository;
import com.av.portfolio_simulator.competition.repository.CompetitionRepository;
import com.av.portfolio_simulator.market.service.MarketService;
import com.av.portfolio_simulator.portfolio.entity.SimulatedPortfolio;
import com.av.portfolio_simulator.portfolio.repository.SimulatedPortfolioRepository;
import com.av.portfolio_simulator.security.UserPrincipal;
import com.av.portfolio_simulator.transaction.repository.HoldingRepository;
import com.av.portfolio_simulator.user.entity.User;
import com.av.portfolio_simulator.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service layer for competition management.
 *
 * When a user joins a competition, a dedicated SimulatedPortfolio is
 * automatically created for them with the competition's initial capital.
 * This portfolio is separate from their personal portfolios.
 *
 * The leaderboard is computed live by fetching current holdings values
 * for each participant and ranking by total return percentage.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionParticipantRepository participantRepository;
    private final SimulatedPortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final UserRepository userRepository;
    private final MarketService marketService;

    /**
     * Creates a new competition. The creator does not automatically join —
     * they must call join separately if they want to participate.
     *
     * @throws BusinessException if end date is not after start date
     */
    @Transactional
    public CompetitionResponse create(CreateCompetitionRequest request, UserPrincipal userPrincipal) {
        if(!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BusinessException("End date must be after start sate");
        }

        User creator = userRepository.findById(userPrincipal.getId()).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );

        Competition competition = Competition.builder()
                .name(request.getName())
                .description(request.getDescription())
                .initialCapital(request.getInitialCapital())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(CompetitionStatus.PENDING)
                .createdBy(creator)
                .build();

        Competition saved = competitionRepository.save(competition);
        return toResponse(saved, 0);

    }

    /**
     * Returns all competitions with a given status.
     * Defaults to ACTIVE if no status is provided.
     */
    public List<CompetitionResponse> getByStatus(CompetitionStatus status) {
        return competitionRepository.findByStatus(status).stream()
            .map(c -> toResponse(c, participantRepository.findByCompetitionId(c.getId()).size()))
            .toList();
    }

    /**
     * Joins an existing competition.
     * Automatically creates a dedicated portfolio for the participant
     * with the competition's initial capital.
     *
     * @throws ResourceNotFoundException if competition not found
     * @throws BusinessException if already joined or competition is not PENDING/ACTIVE
     */
    @Transactional
    public CompetitionResponse join(Long competitionId, UserPrincipal userPrincipal) {
        Competition competition = competitionRepository.findById(competitionId).orElseThrow(
                () -> new ResourceNotFoundException("Competition not found")
        );

        if(competition.getStatus() == CompetitionStatus.FINISHED) {
            throw new BusinessException("Cannot join a finished competition");
        }

        if(participantRepository.existsByCompetitionIdAndUserId(competitionId, userPrincipal.getId())) {
            throw new BusinessException("You have already joined this competition");
        }

        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );

        // Create a dedicated portfolio for this competition
        SimulatedPortfolio competitionPortfolio = SimulatedPortfolio.builder()
                .user(user)
                .name("[Competition] " + competition.getName())
                .initialCapital(competition.getInitialCapital())
                .cashBalance(competition.getInitialCapital())
                .build();
        portfolioRepository.save(competitionPortfolio);

        CompetitionParticipant participant = CompetitionParticipant.builder()
                .competition(competition)
                .user(user)
                .portfolio(competitionPortfolio)
                .build();
        participantRepository.save(participant);

        int count = participantRepository.findByCompetitionId(competitionId).size();
        return toResponse(competition, count);
    }

    /**
     * Joins an existing competition.
     * Automatically creates a dedicated portfolio for the participant
     * with the competition's initial capital.
     *
     * @throws ResourceNotFoundException if already joined or competition is not PENDING/ACTIVE
     */
    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getLeaderboard(Long competitionId){
        competitionRepository.findById(competitionId).orElseThrow(
            () -> new ResourceNotFoundException("Competition not found")
        );

        List<CompetitionParticipant> participants = participantRepository.findByCompetitionId(competitionId);

        // Build and sort entries by total return percent descending
        List<LeaderboardEntry> entries = participants.stream()
            .map(
                participant -> {
                    SimulatedPortfolio portfolio = participant.getPortfolio();
                    BigDecimal holdingsValue = calculateHoldingsValue(portfolio.getId());
                    BigDecimal totalValue = portfolio.getCashBalance().add(holdingsValue);
                    BigDecimal totalReturn = totalValue.subtract(portfolio.getInitialCapital());
                    BigDecimal totalReturnPercent = portfolio.getInitialCapital().compareTo(BigDecimal.ZERO) == 0 ?
                        BigDecimal.ZERO
                            :
                        totalReturn.divide(portfolio.getInitialCapital(), 4, RoundingMode.HALF_UP)
                             .multiply(BigDecimal.valueOf(100))
                             .setScale(2, RoundingMode.HALF_UP);

                    return LeaderboardEntry.builder()
                            .username(participant.getUser().getUsername())
                            .initialCapital(portfolio.getInitialCapital())
                            .currentValue(totalValue.setScale(2, RoundingMode.HALF_UP))
                            .totalReturn(totalReturn.setScale(2, RoundingMode.HALF_UP))
                            .totalReturnPercent(totalReturnPercent)
                            .build();
                }
            )
            .sorted(Comparator.comparing(LeaderboardEntry::getTotalReturnPercent).reversed())
            .toList();

        // Assign ranks after sorting
        AtomicInteger rank = new AtomicInteger(1);
        return entries.stream()
            .map(
                entry -> LeaderboardEntry.builder()
                    .rank(rank.getAndIncrement())
                    .username(entry.getUsername())
                    .initialCapital(entry.getInitialCapital())
                    .currentValue(entry.getCurrentValue())
                    .totalReturn(entry.getTotalReturn())
                    .totalReturnPercent(entry.getTotalReturnPercent())
                    .build()
            )
            .toList();

    }

    /**
     * Calculates the total current market value of all holdings in a portfolio.
     */
    private BigDecimal calculateHoldingsValue(Long portfolioId) {
        var holdings = holdingRepository.findByPortfolioId(portfolioId);
        log.info("Portfolio {} has {} holdings", portfolioId, holdings.size());
        holdings.forEach(h -> log.info("  - Symbol: {}, Quantity: {}", h.getSymbol(), h.getQuantity()));

        return holdings.stream()
                .map(holding -> {
                    try {
                        BigDecimal currentPrice = marketService.getQuote(holding.getSymbol()).getPrice();
                        log.info("  - Price for {}: {}", holding.getSymbol(), currentPrice);
                        return currentPrice.multiply(holding.getQuantity())
                                .setScale(2, RoundingMode.HALF_UP);
                    } catch (Exception e) {
                        log.warn("Failed to fetch price for {}, error: {}, using average buy price as fallback",
                                holding.getSymbol(), e.getMessage());
                        return holding.getAverageBuyPrice().multiply(holding.getQuantity())
                                .setScale(2, RoundingMode.HALF_UP);
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CompetitionResponse toResponse(Competition competition, int participantCount) {
        return CompetitionResponse.builder()
                .id(competition.getId())
                .name(competition.getName())
                .description(competition.getDescription())
                .initialCapital(competition.getInitialCapital())
                .startDate(competition.getStartDate())
                .endDate(competition.getEndDate())
                .status(competition.getStatus())
                .createdBy(competition.getCreatedBy().getUsername())
                .createdAt(competition.getCreatedAt())
                .participantCount(participantCount)
                .build();
    }

}
