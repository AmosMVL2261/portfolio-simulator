package com.av.portfolio_simulator.common.exception;

/**
 * Thrown when a business rule is violated.
 * Maps to HTTP 409 Conflict.
 *
 * Examples:
 * - Insufficient cash balance to buy shares
 * - Duplicate portfolio name
 * - User already joined a competition
 * - Attempting to sell more shares than owned
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

}
