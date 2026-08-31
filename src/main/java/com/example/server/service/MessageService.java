package com.example.server.service;

import com.example.server.model.CreateMessageRequest;
import com.example.server.model.Message;
import com.example.server.repository.MessageRepository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides validation and business operations for messages.
 */
public final class MessageService {
    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = Objects.requireNonNull(messageRepository, "Message repository must not be null");
    }

    public Message create(CreateMessageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }

        String author = requireTrimmedValue(request.author(), "author");
        String content = requireTrimmedValue(request.content(), "content");
        String category = requireTrimmedValue(request.category(), "category");

        Message message = new Message(
                UUID.randomUUID(),
                author,
                content,
                category,
                Instant.now().toEpochMilli()
        );
        return messageRepository.save(message);
    }

    public List<Message> findAll(Optional<String> author) {
        List<Message> messages = messageRepository.findAll();
        if (author == null || author.isEmpty()) {
            return List.copyOf(messages);
        }

        String requestedAuthor = author.get();
        return messages.stream()
                .filter(message -> Objects.equals(message.author(), requestedAuthor))
                .toList();
    }

    public Optional<Message> findById(UUID id) {
        return messageRepository.findById(id);
    }

    public boolean delete(UUID id) {
        return messageRepository.deleteById(id);
    }

    private static String requireTrimmedValue(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(capitalize(fieldName) + " must not be null");
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(capitalize(fieldName) + " must not be blank");
        }
        return trimmed;
    }

    private static String capitalize(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
