package com.example.server.repository;

import com.example.server.model.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Storage abstraction for messages.
 *
 * <p>The repository owns persistence concerns only and has no dependency on
 * transport or serialization frameworks. Implementations must return a
 * snapshot from {@link #findAll()} so callers cannot mutate repository state
 * through a returned collection.</p>
 */
public interface MessageRepository {

    /**
     * Stores a message, including all of its fields such as category.
     *
     * @param message the complete message to store
     * @return the stored message
     */
    Message save(Message message);

    /**
     * Finds a message by its identifier.
     *
     * @param id the message identifier
     * @return the message when present, otherwise an empty optional
     */
    Optional<Message> findById(UUID id);

    /**
     * Returns an independent snapshot of all currently stored messages.
     *
     * @return a snapshot list, never {@code null}
     */
    List<Message> findAll();

    /**
     * Deletes a message by its identifier.
     *
     * @param id the message identifier
     * @return {@code true} when a message was removed, otherwise {@code false}
     */
    boolean deleteById(UUID id);
}
