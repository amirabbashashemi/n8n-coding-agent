package com.example.server.repository;

import com.example.server.model.Message;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation of {@link MessageRepository}.
 *
 * <p>Messages are retained as complete immutable {@link Message} values,
 * including their category. Collection queries operate on a new snapshot so
 * callers cannot observe or modify repository state.</p>
 */
public final class InMemoryMessageRepository implements MessageRepository {

    private static final Comparator<Message> MESSAGE_ORDER =
            Comparator.comparingLong(Message::timestamp)
                    .thenComparing(Message::id);

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
        return Optional.ofNullable(messages.get(id));
    }

    @Override
    public List<Message> findAll() {
        List<Message> snapshot = new ArrayList<>(messages.values());
        snapshot.sort(MESSAGE_ORDER);
        return snapshot;
    }

    @Override
    public List<Message> findByAuthor(String author) {
        List<Message> snapshot = new ArrayList<>();
        for (Message message : messages.values()) {
            if (Objects.equals(message.author(), author)) {
                snapshot.add(message);
            }
        }
        snapshot.sort(MESSAGE_ORDER);
        return snapshot;
    }

    @Override
    public boolean deleteById(UUID id) {
        return messages.remove(id) != null;
    }
}
