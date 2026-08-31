package com.example.server.dto;

/**
 * Public error payload returned by the HTTP API.
 *
 * <p>Handlers should provide a stable, user-facing message rather than an
 * exception message or stack trace. This keeps implementation details out of
 * responses sent to clients.</p>
 *
 * @param error a safe, human-readable description of the error
 */
public record ErrorResponse(String error) {
}
