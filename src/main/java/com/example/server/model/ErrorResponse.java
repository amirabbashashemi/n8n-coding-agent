package com.example.server.model;

import java.util.Objects;

/**
 * Uniform, client-safe representation of an HTTP error response.
 *
 * <p>Handlers should provide deliberately chosen, user-facing messages rather
 * than exception messages or stack traces.</p>
 */
public record ErrorResponse(String error) {

    public ErrorResponse {
        error = Objects.requireNonNull(error, "error must not be null").trim();
        if (error.isEmpty()) {
            throw new IllegalArgumentException("error must not be blank");
        }
    }
}
