package com.av.portfolio_simulator.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Generic wrapper for all API responses.
 * Provides a consistent response structure across all endpoints:
 * {
 *   "success": true/false,
 *   "message": "descriptive message",
 *   "data": { ... } or null
 * }
 *
 * @param <T> the type of the response payload
 */
@Data
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    /**
     * Creates a successful response with a payload.
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Creates an error response with no payload.
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

}
