package com.example.server.model;

/**
 * Consistent, client-safe representation of a controlled HTTP error.
 *
 * @param error a concise, public-facing error message
 */
public record ErrorResponse(String error) {
}
