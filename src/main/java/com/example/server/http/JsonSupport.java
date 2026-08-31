package com.example.server.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Objects;

/**
 * Centralizes JSON serialization and deserialization for HTTP handlers.
 * A single configured mapper keeps the wire format consistent throughout the
 * application and prevents callers from having to depend on Jackson directly.
 */
public final class JsonSupport {
    private final ObjectMapper objectMapper;

    /** Creates JSON support using the server's standard mapper configuration. */
    public JsonSupport() {
        this(new ObjectMapper());
    }

    /**
     * Creates JSON support with a caller-supplied mapper, primarily for
     * applications that need to register additional modules.
     *
     * @param objectMapper mapper used for all operations
     */
    public JsonSupport(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Deserializes a JSON document into the requested type.
     *
     * @throws IllegalArgumentException when the document is invalid or cannot
     *                                  be mapped to {@code type}
     */
    public <T> T fromJson(String json, Class<T> type) {
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(type, "type");
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid JSON request body", exception);
        }
    }

    /**
     * Serializes a value into a compact JSON document.
     *
     * @throws IllegalArgumentException when the value cannot be serialized
     */
    public String toJson(Object value) {
        Objects.requireNonNull(value, "value");
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Unable to serialize response", exception);
        }
    }
}
