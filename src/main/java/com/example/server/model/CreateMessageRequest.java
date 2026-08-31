package com.example.server.model;

/**
 * Request payload used when creating a message.
 *
 * <p>The server assigns the message identifier and timestamp, so they are
 * intentionally not part of this input model.</p>
 */
public record CreateMessageRequest(String author, String content, String category) {
}
