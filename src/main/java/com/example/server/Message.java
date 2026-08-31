package com.example.server;

import java.util.UUID;

/**
 * Immutable domain model representing a message.
 *
 * @param id unique identifier of the message
 * @param author author of the message
 * @param content message content
 * @param category required textual category of the message
 * @param timestamp message creation timestamp
 */
public record Message(
        UUID id,
        String author,
        String content,
        String category,
        long timestamp) {
}
