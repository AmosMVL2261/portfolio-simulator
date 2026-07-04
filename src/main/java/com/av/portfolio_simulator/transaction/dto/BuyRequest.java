package com.av.portfolio_simulator.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body for placing a simulated buy order.
 */
@Data
public class BuyRequest {

    @NotBlank(message = "Symbol is required")
    @Size(max = 20, message = "Symbol must not exceed 20 characters")
    private String symbol;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.000001", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

}
