package com.av.portfolio_simulator.common.exception;

/**
 * Thrown when a requested resource does not exist in the system.
 * Maps to HTTP 404 Not Found.
 *
 * Examples:
 * - Portfolio not found by ID
 * - Competition not found by ID
 * - Symbol not found in market data
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found with id: " + id);
    }

}
