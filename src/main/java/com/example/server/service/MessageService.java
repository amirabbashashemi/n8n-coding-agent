package com.example.server.service;

import com.example.server.model.CreateMessageRequest;
import com.example.server.model.Message;
import com.example.server.repository.MessageRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Application service for validating and managing messages.
 *
 * <p>This class contains no transport concerns; callers are responsible for
 * translating domain failures into an appropriate protocol response.</p>
 */
public final class MessageService {
    private final MessageRepository repository;

    public MessageService(MessageRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    /**
     * Validates, normalizes, creates, and persists a message.
     *
     * @param request request containing the message fields
     * @return the persisted message
     * @throws InvalidMessageException if the request or any required field is null or blank
     */
    public Message create(CreateMessageRequest request) {
        if (request == null) {
            throw new InvalidMessageException("request must not be null");
        }

        String author = requireText(request.author(), "author");
        String content = requireText(request.content(), "content");
        String category = requireText(request.category(), "category");

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
     * Returns all messages, optionally filtered by an exact normalized author.
     * A null or blank filter means that no filter is applied.
     */
    public List<Message> findAll(String author) {
        if (author == null || author.isBlank()) {
            return repository.findAll();
        }
        return repository.findByAuthor(author.trim());
    }

    /** Returns the message identified by {@code id}, if it exists. */
    public java.util.Optional<Message> findById(UUID id) {
        if (id == null) {
            throw new InvalidMessageException("id must not be null");
        }
        return repository.findById(id);
    }

    /** Deletes a message and reports whether a matching record existed. */
    public boolean deleteById(UUID id) {
        if (id == null) {
            throw new InvalidMessageException("id must not be null");
        }
        return repository.deleteById(id);
    }

    // Descriptive service-level aliases for callers using operation-oriented names.
    public List<Message> getMessages(String author) {
        return findAll(author);
    }

    public java.util.Optional<Message> getMessage(UUID id) {
        return findById(id);
    }

    public boolean deleteMessage(UUID id) {
        return deleteById(id);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null) {
            throw new InvalidMessageException(fieldName + " must not be null");
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new InvalidMessageException(fieldName + " must not be blank");
        }
        return normalized;
    }

    /** Domain-level validation failure for an invalid message request. */
    public static final class InvalidMessageException extends IllegalArgumentException {
        public InvalidMessageException(String message) {
            super(message);
        }
    }
}
