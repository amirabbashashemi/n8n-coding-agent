package com.example.server.model;

import java.util.UUID;

/**
 * Immutable domain model representing a message.
 *
 * @param id unique identifier of the message
 * @param author message author
 * @param content message content
 * @param category message category
 * @param timestamp creation timestamp in milliseconds since the Unix epoch
 */
public record Message(
        UUID id,
        String author,
        String content,
        String category,
        long timestamp
) {
}
