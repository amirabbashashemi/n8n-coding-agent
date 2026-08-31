package com.example.server.http;

import com.example.server.model.ErrorResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

/**
 * Handles the service health endpoint.
 *
 * <p>The handler deliberately accepts only the exact {@code /api/health}
 * path and the GET method. It delegates JSON serialization and response
 * writing to the shared {@link JsonHttpResponder}.</p>
 */
public final class HealthHandler implements HttpHandler {
    private static final String HEALTH_PATH = "/api/health";
    private static final String ALLOW_HEADER = "GET";

    private final JsonHttpResponder responder;

    public HealthHandler(JsonHttpResponder responder) {
        this.responder = responder;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!HEALTH_PATH.equals(exchange.getRequestURI().getPath())) {
            responder.sendJson(exchange, 404, new ErrorResponse("Not found"));
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Allow", ALLOW_HEADER);
            responder.sendJson(exchange, 405, new ErrorResponse("Method not allowed"));
            return;
        }

        responder.sendJson(exchange, 200, Map.of("status", "UP"));
    }
}
