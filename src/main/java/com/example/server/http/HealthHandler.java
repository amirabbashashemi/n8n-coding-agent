package com.example.server.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Handles the service health endpoint without depending on application state.
 */
public final class HealthHandler implements HttpHandler {

    private static final String CONTENT_TYPE = "application/json; charset=utf-8";
    private static final byte[] HEALTH_RESPONSE = "{\"status\":\"UP\"}".getBytes(StandardCharsets.UTF_8);
    private static final byte[] METHOD_NOT_ALLOWED_RESPONSE =
            "{\"error\":\"METHOD_NOT_ALLOWED\",\"message\":\"Only GET is supported\"}"
                    .getBytes(StandardCharsets.UTF_8);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Allow", "GET");
                writeResponse(exchange, 405, METHOD_NOT_ALLOWED_RESPONSE);
                return;
            }

            writeResponse(exchange, 200, HEALTH_RESPONSE);
        }
    }

    private static void writeResponse(HttpExchange exchange, int statusCode, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", CONTENT_TYPE);
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
