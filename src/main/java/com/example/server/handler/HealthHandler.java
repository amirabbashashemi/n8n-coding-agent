package com.example.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Handles health checks for the application.
 */
public final class HealthHandler implements HttpHandler {

    private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
    private static final byte[] HEALTH_RESPONSE = "{\"status\":\"UP\"}".getBytes(StandardCharsets.UTF_8);
    private static final byte[] METHOD_NOT_ALLOWED_RESPONSE = "{\"error\":\"Method Not Allowed\"}".getBytes(StandardCharsets.UTF_8);
    private static final byte[] INTERNAL_SERVER_ERROR_RESPONSE = "{\"error\":\"Internal Server Error\"}".getBytes(StandardCharsets.UTF_8);

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Allow", "GET");
                sendJson(exchange, 405, METHOD_NOT_ALLOWED_RESPONSE);
                return;
            }

            sendJson(exchange, 200, HEALTH_RESPONSE);
        } catch (Exception ignored) {
            try {
                sendJson(exchange, 500, INTERNAL_SERVER_ERROR_RESPONSE);
            } catch (IOException ignoredFailure) {
                // The exchange may already be closed or committed; there is no safe
                // response operation left in that case.
            }
        }
    }

    private static void sendJson(HttpExchange exchange, int statusCode, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", JSON_CONTENT_TYPE);
        exchange.sendResponseHeaders(statusCode, body.length);
        try (var responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
