package com.example.server.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * The single JSON boundary used by the HTTP layer.
 *
 * <p>The mapper is fully configured during construction and is subsequently
 * only used for its thread-safe read and write operations.  In particular,
 * the mapper supplied by a caller is copied before configuration so that
 * constructing this class cannot mutate shared application configuration.</p>
 */
public final class JsonSupport {
    public static final String CONTENT_TYPE = "application/json; charset=utf-8";

    private final ObjectMapper objectMapper;

    /** Creates JSON support with Jackson's standard mapper configuration. */
    public JsonSupport() {
        this(new ObjectMapper());
    }

    /**
     * Creates JSON support from the supplied mapper.
     *
     * @param objectMapper mapper to use as the base configuration
     * @throws NullPointerException if {@code objectMapper} is null
     */
    public JsonSupport(ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper, "objectMapper");
        this.objectMapper = objectMapper.copy()
                // Records are supported by Jackson's standard introspection;
                // this also keeps the behavior explicit for all DTO records.
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /** Serializes a value as a UTF-8 JSON string. */
    public String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new JsonException("Unable to serialize JSON", exception);
        }
    }

    /** Serializes a value as UTF-8 JSON bytes suitable for an HTTP response. */
    public byte[] serializeBytes(Object value) {
        return serialize(value).getBytes(StandardCharsets.UTF_8);
    }

    /** Deserializes a JSON string into the requested DTO type. */
    public <T> T deserialize(String json, Class<T> targetType) {
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(targetType, "targetType");
        try {
            return objectMapper.readValue(json, targetType);
        } catch (IOException | RuntimeException exception) {
            throw asJsonException("Unable to deserialize JSON", exception);
        }
    }

    /** Deserializes a UTF-8 JSON request body into the requested DTO type. */
    public <T> T deserialize(InputStream body, Class<T> targetType) {
        Objects.requireNonNull(body, "body");
        Objects.requireNonNull(targetType, "targetType");
        try {
            return objectMapper.readValue(body, targetType);
        } catch (IOException | RuntimeException exception) {
            throw asJsonException("Unable to deserialize JSON", exception);
        }
    }

    /** Returns the content type used for every JSON response. */
    public String contentType() {
        return CONTENT_TYPE;
    }

    /** Exception raised for either JSON serialization or deserialization failures. */
    public static final class JsonException extends RuntimeException {
        public JsonException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static JsonException asJsonException(String message, Throwable exception) {
        if (exception instanceof JsonException jsonException) {
            return jsonException;
        }
        return new JsonException(message, exception);
    }
}
