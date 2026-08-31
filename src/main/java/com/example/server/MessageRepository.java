package com.example.server;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence abstraction for messages.
 *
 * <p>The HTTP layer depends on this contract rather than on a particular
 * storage implementation. Implementations are responsible for preserving the
 * complete {@link Message}, including its category.</p>
 */
public interface MessageRepository {

    /**
     * Stores a message and returns the stored value.
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
     * Lists all stored messages.
     *
     * @return all messages, never {@code null}
     */
    List<Message> findAll();

    /**
     * Lists messages written by the specified author.
     *
     * @param author author to match
     * @return matching messages, never {@code null}
     */
    List<Message> findByAuthor(String author);

    /**
     * Removes a message by its identifier.
     *
     * @param id message identifier
     * @return {@code true} if a message was removed
     */
    boolean deleteById(UUID id);
}
