package com.example.server.service;

import com.example.server.dto.CreateMessageRequest;
import com.example.server.model.Message;
import com.example.server.repository.MessageRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides the business operations for messages.
 *
 * <p>Incoming text values are normalized by trimming leading and trailing
 * whitespace. A value is rejected when it is {@code null} or empty after
 * trimming; the normalized value is what is stored in the resulting message.
 * Timestamps are generated as epoch milliseconds.</p>
 */
public final class MessageService {

    private final MessageRepository repository;

    public MessageService(MessageRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    /**
     * Validates and creates a message with server-generated identity and time.
     *
     * @param request the client-supplied message fields
     * @return the message persisted by the repository
     * @throws IllegalArgumentException when the request or a required field is invalid
     */
    public Message create(CreateMessageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        String author = requiredField(request.author(), "author");
        String content = requiredField(request.content(), "content");
        String category = requiredField(request.category(), "category");

        Message message = new Message(
                UUID.randomUUID(),
                author,
                content,
                category,
                System.currentTimeMillis()
        );
        return repository.save(message);
    }

    /**
     * Lists messages, optionally restricting the result to an exact author.
     * A {@code null} author means that no author filter is applied.
     */
    public List<Message> list(String author) {
        return author == null ? repository.findAll() : repository.findByAuthor(author);
    }

    /**
     * Finds a message by its identifier.
     *
     * @param id message identifier, or {@code null}
     * @return the matching message, if present
     */
    public Optional<Message> get(UUID id) {
        return id == null ? Optional.empty() : repository.findById(id);
    }

    /**
     * Deletes a message by its identifier.
     *
     * @param id message identifier, or {@code null}
     * @return {@code true} when a message was removed
     */
    public boolean delete(UUID id) {
        return id != null && repository.deleteById(id);
    }

    private static String requiredField(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
        return normalized;
    }
}
