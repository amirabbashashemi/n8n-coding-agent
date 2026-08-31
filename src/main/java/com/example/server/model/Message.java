package com.example.server.model;

import java.util.UUID;

/**
 * Immutable domain representation of a message.
 *
 * @param id unique identifier of the message
 * @param author author of the message
 * @param content textual content of the message
 * @param category required textual category of the message
 * @param timestamp creation time represented as epoch milliseconds since 1970-01-01T00:00:00Z
 */
public record Message(
        UUID id,
        String author,
        String content,
        String category,
        long timestamp
) {
}
