package com.example.server;

/**
 * Standard JSON representation for an HTTP error response.
 *
 * @param error a client-safe description of the error
 */
public record ErrorResponse(String error) {
}
