package com.example.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * HTTP handler for the service health endpoint.
 *
 * <p>The handler intentionally has no dependencies on the message service or
 * repository so that health checks remain available independently of the
 * application data layer.</p>
 */
public final class HealthHandler implements HttpHandler {

    private static final String CONTENT_TYPE = "application/json; charset=utf-8";
    private static final byte[] HEALTH_RESPONSE = "{\"status\":\"UP\"}"
            .getBytes(StandardCharsets.UTF_8);
    private static final byte[] METHOD_NOT_ALLOWED_RESPONSE = "{\"error\":\"Method Not Allowed\"}"
            .getBytes(StandardCharsets.UTF_8);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 200, HEALTH_RESPONSE);
            } else {
                exchange.getResponseHeaders().set("Allow", "GET");
                sendJson(exchange, 405, METHOD_NOT_ALLOWED_RESPONSE);
            }
        } finally {
            exchange.close();
        }
    }

    private static void sendJson(HttpExchange exchange, int statusCode, byte[] body)
            throws IOException {
        exchange.getResponseHeaders().set("Content-Type", CONTENT_TYPE);
        exchange.sendResponseHeaders(statusCode, body.length);
        exchange.getResponseBody().write(body);
    }
}
