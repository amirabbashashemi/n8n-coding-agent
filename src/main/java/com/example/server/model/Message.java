package com.example.server.model;

import java.util.UUID;

/**
 * Immutable domain representation of a message.
 *
 * @param id unique identifier of the message
 * @param author normalized author name
 * @param content normalized message content
 * @param category normalized message category
 * @param timestamp creation timestamp, represented as epoch milliseconds
 */
public record Message(
        UUID id,
        String author,
        String content,
        String category,
        long timestamp
) {
}
