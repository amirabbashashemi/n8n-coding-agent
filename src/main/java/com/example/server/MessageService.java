package com.example.server;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service for creating and managing messages.
 *
 * <p>The service owns input validation and server-side metadata generation so
 * callers cannot accidentally persist incomplete messages or client-supplied
 * identifiers and timestamps.</p>
 */
public final class MessageService {
    private final MessageRepository repository;

    public MessageService(MessageRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    /**
     * Validates the supplied fields, creates a complete message, and stores it.
     *
     * @throws IllegalArgumentException when any required field is null or blank
     */
    public Message createMessage(String author, String content, String category) {
        validateRequired("author", author);
        validateRequired("content", content);
        validateRequired("category", category);

        Message message = new Message(
                UUID.randomUUID(),
                author,
                content,
                category,
                System.currentTimeMillis());
        return repository.save(message);
    }

    public Optional<Message> getMessage(UUID id) {
        return repository.findById(Objects.requireNonNull(id, "id"));
    }

    public List<Message> getMessages() {
        return repository.findAll();
    }

    public List<Message> getMessagesByAuthor(String author) {
        validateRequired("author", author);
        return repository.findByAuthor(author);
    }

    public boolean deleteMessage(UUID id) {
        return repository.deleteById(Objects.requireNonNull(id, "id"));
    }

    private static void validateRequired(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
