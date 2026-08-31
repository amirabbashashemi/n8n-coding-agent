package com.example.server.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Writes JSON responses consistently for the HTTP handlers in this service.
 *
 * <p>The supplied mapper is deliberately shared by all handlers, so response
 * models (including {@code Message} and the health response) are serialized
 * using the same Jackson configuration.</p>
 */
public final class JsonHttpResponder {
    private static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";

    private final ObjectMapper objectMapper;

    public JsonHttpResponder(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    /**
     * Serializes {@code body} and sends it as a JSON response.
     *
     * @param exchange the exchange receiving the response
     * @param statusCode HTTP status code
     * @param body response value to serialize
     * @throws IOException if serialization or response writing fails
     */
    public void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        Objects.requireNonNull(exchange, "exchange");
        Objects.requireNonNull(body, "body");

        byte[] payload = objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", JSON_CONTENT_TYPE);
        exchange.getResponseHeaders().set("Content-Length", Long.toString(payload.length));
        exchange.sendResponseHeaders(statusCode, payload.length);
        try {
            exchange.getResponseBody().write(payload);
        } finally {
            exchange.close();
        }
    }

    /**
     * Sends a successful no-content response without a response body.
     *
     * @param exchange the exchange receiving the response
     * @throws IOException if the response cannot be sent
     */
    public void sendNoContent(HttpExchange exchange) throws IOException {
        Objects.requireNonNull(exchange, "exchange");
        exchange.getResponseHeaders().remove("Content-Type");
        exchange.getResponseHeaders().remove("Content-Length");
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }
}
