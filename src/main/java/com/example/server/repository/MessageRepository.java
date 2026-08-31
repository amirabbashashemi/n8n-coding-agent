package com.example.server.repository;

import com.example.server.model.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence contract for messages.
 *
 * <p>Implementations must not expose their mutable internal state through the
 * collection-returning methods. Each returned list must be a snapshot or an
 * otherwise unmodifiable collection.</p>
 */
public interface MessageRepository {

    /**
     * Stores a message and returns the stored value.
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
     * Returns a snapshot of all messages.
     *
     * @return an immutable or detached list of messages
     */
    List<Message> findAll();

    /**
     * Returns a snapshot of messages written by the specified author.
     *
     * @param author the exact author value to match
     * @return an immutable or detached list of matching messages
     */
    List<Message> findByAuthor(String author);

    /**
     * Deletes a message by its identifier.
     *
     * @param id the message identifier
     * @return {@code true} when a message was deleted
     */
    boolean deleteById(UUID id);
}
