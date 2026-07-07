package com.av.portfolio_simulator.competition.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request body for creating a new competition.
 */
@Data
public class CreateCompetitionRequest {

    @NotBlank(message = "Competition name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Initial capital is required")
    @DecimalMin(value = "100.00", message = "Initial capital must be at least 100.00")
    private BigDecimal initialCapital;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;

}
