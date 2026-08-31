package com.example.server.service;

import com.example.server.model.Message;
import com.example.server.repository.MessageRepository;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides the domain operations for messages and applies the service-level
 * ordering contract independently of the repository implementation.
 */
public final class MessageService {
    private static final Comparator<Message> MESSAGE_ORDER =
            Comparator.comparingLong(Message::timestamp)
                    .thenComparing(Message::id);

    private final MessageRepository repository;

    public MessageService(MessageRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    /**
     * Creates and persists a complete message using a random identifier and
     * the current UTC epoch time in milliseconds.
     */
    public Message create(String author, String content, String category) {
        Message message = new Message(
                UUID.randomUUID(),
                author,
                content,
                category,
                Instant.now().toEpochMilli());
        return repository.save(message);
    }

    /** Returns all messages in deterministic timestamp/UUID order. */
    public List<Message> findAll() {
        return ordered(repository.findAll());
    }

    /** Returns a message by identifier, when it exists. */
    public Optional<Message> findById(UUID id) {
        return repository.findById(id);
    }

    /** Returns messages authored by the supplied author in deterministic order. */
    public List<Message> findByAuthor(String author) {
        return ordered(repository.findAll().stream()
                .filter(message -> Objects.equals(message.author(), author))
                .toList());
    }

    /** Deletes a message and reports whether an existing message was removed. */
    public boolean deleteById(UUID id) {
        return repository.deleteById(id);
    }

    private static List<Message> ordered(Iterable<Message> messages) {
        return java.util.stream.StreamSupport.stream(messages.spliterator(), false)
                .sorted(MESSAGE_ORDER)
                .toList();
    }
}
