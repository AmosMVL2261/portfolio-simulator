package com.av.portfolio_simulator.portfolio.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body for creating a new simulated portfolio.
 */
@Data
public class CreatePortfolioRequest {

    @NotBlank(message = "Portfolio name is required")
    @Size(min = 3, max = 100, message = "Portfolio name must be between 3 and 100 characters")
    private String name;

    @NotNull(message = "Initial capital is required")
    @DecimalMin(value = "100.00", message = "Initial capital must be at least 100.00")
    @DecimalMax(value = "10000000.00", message = "Initial capital cannot exceed 10,000,000.00")
    private BigDecimal initialCapital;

}
