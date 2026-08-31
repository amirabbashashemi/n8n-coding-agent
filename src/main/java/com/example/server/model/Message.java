package com.example.server.model;

import java.util.UUID;

/**
 * Immutable domain model representing a message.
 *
 * @param id unique message identifier
 * @param author message author
 * @param content message content
 * @param category message category
 * @param timestamp message creation time in epoch milliseconds
 */
public record Message(
        UUID id,
        String author,
        String content,
        String category,
        long timestamp) {
}
