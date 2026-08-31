package com.example.server.http;

import com.example.server.dto.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/** Centralizes JSON serialization and HTTP response formatting for the server. */
public final class JsonSupport {
    public static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

    private final ObjectMapper objectMapper;

    /** Creates JSON support with the application's standard Jackson configuration. */
    public JsonSupport() {
        this(createObjectMapper());
    }

    /** Creates JSON support around an application-provided mapper. */
    public JsonSupport(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    /** Creates the mapper used by the HTTP layer. */
    public static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    /** Returns the configured mapper for integration with components needing Jackson directly. */
    public ObjectMapper objectMapper() {
        return objectMapper;
    }

    /** Deserializes a request body and wraps malformed JSON and I/O failures. */
    public <T> T read(InputStream body, Class<T> type) {
        Objects.requireNonNull(body, "body");
        Objects.requireNonNull(type, "type");
        try {
            return objectMapper.readValue(body, type);
        } catch (JsonProcessingException exception) {
            throw new JsonSupportException("Request body is not valid JSON", exception);
        } catch (IOException exception) {
            throw new JsonSupportException("Unable to read request body", exception);
        }
    }

    /** Serializes a value and wraps Jackson failures. */
    public String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new JsonSupportException("Unable to serialize JSON response", exception);
        }
    }

    /** Sends a JSON response with the common content type and supplied HTTP status. */
    public void sendJson(HttpExchange exchange, int status, Object value) {
        Objects.requireNonNull(exchange, "exchange");
        byte[] body = write(value).getBytes(StandardCharsets.UTF_8);
        sendBytes(exchange, status, body, true);
    }

    /** Sends the standard error envelope used by every HTTP endpoint. */
    public void sendError(HttpExchange exchange, int status, String message) {
        String safeMessage = message == null || message.isBlank()
                ? "Request could not be processed"
                : message;
        sendJson(exchange, status, new ErrorResponse(safeMessage));
    }

    /** Sends the common health response: {"status":"UP"}. */
    public void sendHealth(HttpExchange exchange) {
        sendJson(exchange, 200, Map.of("status", "UP"));
    }

    /** Sends a response with no body, useful for HTTP 204 responses. */
    public void sendNoContent(HttpExchange exchange, int status) {
        Objects.requireNonNull(exchange, "exchange");
        sendBytes(exchange, status, new byte[0], false);
    }

    private void sendBytes(HttpExchange exchange, int status, byte[] body, boolean json) {
        if (json) {
            exchange.getResponseHeaders().set("Content-Type", JSON_CONTENT_TYPE);
        }
        try {
            exchange.sendResponseHeaders(status, body.length);
            try (var responseBody = exchange.getResponseBody()) {
                responseBody.write(body);
            }
        } catch (IOException exception) {
            throw new JsonSupportException("Unable to send HTTP response", exception);
        }
    }

    /** Runtime exception keeping JSON and response I/O failures identifiable to handlers. */
    public static final class JsonSupportException extends RuntimeException {
        public JsonSupportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
