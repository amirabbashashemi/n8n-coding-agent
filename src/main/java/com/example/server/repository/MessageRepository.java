package com.example.server.repository;

import com.example.server.model.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository contract for persisting and retrieving messages.
 */
public interface MessageRepository {

    /**
     * Stores a message and returns the stored message.
     *
     * @param message the complete message, including its category
     * @return the stored message
     */
    Message save(Message message);

    /**
     * Finds a message by its identifier.
     *
     * @param id the message identifier
     * @return the message when present
     */
    Optional<Message> findById(UUID id);

    /**
     * Returns an independent snapshot of all stored messages.
     *
     * @return a list that is independent of the repository's internal storage
     */
    List<Message> findAll();

    /**
     * Removes a message by its identifier.
     *
     * @param id the message identifier
     * @return {@code true} when a message was removed
     */
    boolean deleteById(UUID id);
}
