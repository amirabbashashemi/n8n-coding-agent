package com.example.server.http;

import com.example.server.dto.ErrorResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Handles the exact {@code GET /api/health} endpoint.
 *
 * <p>The handler contains no mutable request-specific state and can therefore
 * safely be shared by all server worker threads.</p>
 */
public final class HealthHandler implements HttpHandler {
    private static final String PATH = "/api/health";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

    private final JsonSupport jsonSupport;

    public HealthHandler(JsonSupport jsonSupport) {
        this.jsonSupport = java.util.Objects.requireNonNull(jsonSupport, "jsonSupport");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!PATH.equals(exchange.getRequestURI().getPath())) {
                sendJson(exchange, 404, new ErrorResponse("Resource not found"));
                return;
            }

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Allow", "GET");
                sendJson(exchange, 405, new ErrorResponse("Method not allowed"));
                return;
            }

            sendJson(exchange, 200, Map.of("status", "UP"));
        } finally {
            exchange.close();
        }
    }

    private void sendJson(HttpExchange exchange, int status, Object payload) throws IOException {
        byte[] response = jsonSupport.write(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", JSON_CONTENT_TYPE);
        exchange.getResponseHeaders().set("Content-Length", Long.toString(response.length));
        exchange.sendResponseHeaders(status, response.length);
        try (var output = exchange.getResponseBody()) {
            output.write(response);
            output.flush();
        }
    }
}
