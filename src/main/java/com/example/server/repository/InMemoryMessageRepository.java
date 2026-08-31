package com.example.server.repository;

import com.example.server.model.Message;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation of the message repository.
 */
public final class InMemoryMessageRepository implements MessageRepository {
    private final ConcurrentHashMap<UUID, Message> messages = new ConcurrentHashMap<>();

    @Override
    public Message save(Message message) {
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(message.id(), "message id must not be null");
        messages.put(message.id(), message);
        return message;
    }

    @Override
    public Optional<Message> findById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(messages.get(id));
    }

    @Override
    public List<Message> findAll() {
        return List.copyOf(messages.values());
    }

    @Override
    public boolean deleteById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return messages.remove(id) != null;
    }
}
