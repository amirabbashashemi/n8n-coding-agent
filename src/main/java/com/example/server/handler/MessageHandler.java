package com.example.server.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

/**
 * Parses and normalises inbound JSON messages for the message server.
 *
 * <p>The handler deliberately works with Jackson's tree model so that the
 * transport layer does not need to depend on a concrete message DTO. This
 * also keeps the wire format stable when new message fields are introduced.</p>
 */
public final class MessageHandler {
    private final ObjectMapper objectMapper;

    public MessageHandler() {
        this(new ObjectMapper());
    }

    public MessageHandler(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    /**
     * Validates an inbound JSON document and returns its canonical JSON form.
     *
     * @param payload inbound UTF-8 JSON text
     * @return compact JSON containing the original message fields
     * @throws IllegalArgumentException when the payload is empty or not JSON
     */
    public String handle(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Message payload must not be blank");
        }
        try {
            JsonNode message = objectMapper.readTree(payload);
            if (message == null || !message.isObject()) {
                throw new IllegalArgumentException("Message payload must be a JSON object");
            }
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Message payload is not valid JSON", exception);
        }
    }

    /**
     * Parses a payload without changing its representation.
     *
     * @param payload inbound JSON text
     * @return parsed message tree
     * @throws IllegalArgumentException when the payload is invalid
     */
    public JsonNode parse(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Message payload must not be blank");
        }
        try {
            JsonNode message = objectMapper.readTree(payload);
            if (message == null || !message.isObject()) {
                throw new IllegalArgumentException("Message payload must be a JSON object");
            }
            return message;
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Message payload is not valid JSON", exception);
        }
    }
}
