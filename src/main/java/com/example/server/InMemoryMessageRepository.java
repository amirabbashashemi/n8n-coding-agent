package com.example.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory implementation of the message repository.
 */
public final class InMemoryMessageRepository implements MessageRepository {
    private final ConcurrentMap<UUID, Message> messages = new ConcurrentHashMap<>();

    @Override
    public Message save(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
        if (message.id() == null) {
            throw new IllegalArgumentException("message id must not be null");
        }
        messages.put(message.id(), message);
        return message;
    }

    @Override
    public Optional<Message> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(messages.get(id));
    }

    @Override
    public List<Message> findAll() {
        return new ArrayList<>(messages.values());
    }

    @Override
    public List<Message> findByAuthor(String author) {
        if (author == null) {
            return List.of();
        }
        return messages.values().stream()
                .filter(message -> author.equals(message.author()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(UUID id) {
        return id != null && messages.remove(id) != null;
    }
}
