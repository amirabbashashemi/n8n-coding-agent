package com.example.server.http;

import com.example.server.service.MessageService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * HTTP endpoint for reading messages.
 *
 * <p>The message service exposes a single filtered query operation,
 * {@code findAll(String)}.  The optional {@code author} query parameter is
 * passed directly to that operation; a missing parameter means that no
 * author filter is applied.</p>
 */
public final class MessageHandler implements HttpHandler {
    private final MessageService messageService;

    public MessageHandler(MessageService messageService) {
        if (messageService == null) {
            throw new IllegalArgumentException("messageService must not be null");
        }
        this.messageService = messageService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Allow", "GET");
                send(exchange, 405, "{\"error\":\"method not allowed\"}");
                return;
            }

            String author = queryParameters(exchange.getRequestURI().getRawQuery()).get("author");
            List<?> messages = messageService.findAll(author);
            send(exchange, 200, toJson(messages));
        } catch (RuntimeException exception) {
            send(exchange, 500, "{\"error\":\"internal server error\"}");
        } finally {
            exchange.close();
        }
    }

    private static Map<String, String> queryParameters(String query) {
        if (query == null || query.isBlank()) {
            return Map.of();
        }
        java.util.HashMap<String, String> parameters = new java.util.HashMap<>();
        for (String pair : query.split("&")) {
            int separator = pair.indexOf('=');
            String key = separator < 0 ? pair : pair.substring(0, separator);
            String value = separator < 0 ? "" : pair.substring(separator + 1);
            parameters.put(urlDecode(key), urlDecode(value));
        }
        return parameters;
    }

    private static String urlDecode(String value) {
        return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String toJson(List<?> messages) {
        StringBuilder json = new StringBuilder("[");
        for (int index = 0; index < messages.size(); index++) {
            if (index > 0) {
                json.append(',');
            }
            Object message = messages.get(index);
            if (message == null) {
                json.append("null");
            } else {
                json.append('"').append(escape(String.valueOf(message))).append('"');
            }
        }
        return json.append(']').toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, payload.length);
        try (var output = exchange.getResponseBody()) {
            output.write(payload);
        }
    }
}
