package com.example.server.model;

/**
 * Payload accepted when a client creates a message.
 *
 * @param author message author
 * @param content message body
 * @param category optional message category
 */
public record CreateMessageRequest(String author, String content, String category) {
}
