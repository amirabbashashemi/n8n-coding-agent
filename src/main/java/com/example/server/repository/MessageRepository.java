package com.example.server.repository;

import com.example.server.model.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence contract for messages.
 *
 * <p>A message's category is persisted and returned as part of the
 * {@link Message} value; it does not have a separate repository operation.
 * Implementations must return collection snapshots (or otherwise
 * independently modifiable collections) from the collection-returning
 * methods so that internal storage is never exposed to callers.</p>
 */
public interface MessageRepository {

    /**
     * Stores a message, replacing any existing message with the same id.
     *
     * @param message message to store
     * @return the stored message
     */
    Message save(Message message);

    /**
     * Finds a message by its identifier.
     *
     * @param id message identifier
     * @return the message when present
     */
    Optional<Message> findById(UUID id);

    /**
     * Returns a snapshot of all stored messages.
     *
     * @return independently modifiable collection containing all messages
     */
    List<Message> findAll();

    /**
     * Returns a snapshot of messages whose author exactly matches the given
     * author.
     *
     * @param author author to match
     * @return independently modifiable collection of matching messages
     */
    List<Message> findByAuthor(String author);

    /**
     * Deletes a message by its identifier.
     *
     * @param id message identifier
     * @return {@code true} if a message was removed, otherwise {@code false}
     */
    boolean deleteById(UUID id);
}
