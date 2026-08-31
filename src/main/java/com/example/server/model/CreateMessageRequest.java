package com.example.server.model;

/**
 * Request payload used to create a message.
 *
 * @param author   the message author
 * @param content  the message content
 * @param category the message category
 */
public record CreateMessageRequest(String author, String content, String category) {
}
