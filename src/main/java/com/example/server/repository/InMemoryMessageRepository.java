package com.example.server.repository;

import com.example.server.model.Message;

import java.util.ArrayList;
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
        UUID id = Objects.requireNonNull(message.id(), "message.id must not be null");
        messages.put(id, message);
        return message;
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return Optional.ofNullable(messages.get(Objects.requireNonNull(id, "id must not be null")));
    }

    @Override
    public List<Message> findAll() {
        return snapshot().stream().toList();
    }

    @Override
    public List<Message> findByAuthor(String author) {
        Objects.requireNonNull(author, "author must not be null");
        return snapshot().stream()
                .filter(message -> author.equals(message.author()))
                .toList();
    }

    @Override
    public boolean deleteById(UUID id) {
        return messages.remove(Objects.requireNonNull(id, "id must not be null")) != null;
    }

    private List<Message> snapshot() {
        return List.copyOf(new ArrayList<>(messages.values()));
    }
}
