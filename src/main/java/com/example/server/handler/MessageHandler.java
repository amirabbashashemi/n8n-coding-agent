package com.example.server.handler;

import com com.example.server.model.CreateMessageRequest;
import com.example.server.model.ErrorResponse;
import com.example.server.service.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class MessageHandler implements HttpHandler {
    private static final String COLLECTION_PATH = "/api/messages";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    public MessageHandler(MessageService messageService, ObjectMapper objectMapper) {
        this.messageService = messageService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if (COLLECTION_PATH.equals(path)) {
                if ("POST".equalsIgnoreCase(method)) {
                    handleCreate(exchange);
                } else if ("GET".equalsIgnoreCase(method)) {
                    sendJson(exchange, 200, messageService.findAll(parseAuthorQuery(exchange.getRequestURI())));
                } else {
                    exchange.getResponseHeaders().set("Allow", "GET, POST");
                    sendError(exchange, 405, "Method not allowed");
                }
                return;
            }

            if (path != null && path.startsWith(COLLECTION_PATH + "/")
                    && path.length() > COLLECTION_PATH.length() + 1
                    && path.indexOf('/', COLLECTION_PATH.length() + 1) < 0) {
                UUID id = parseMessageId(path.substring(COLLECTION_PATH.length() + 1));
                if ("GET".equalsIgnoreCase(method)) {
                    messageService.findById(id)
                            .ifPresentOrElse(message -> sendJsonUnchecked(exchange, 200, message),
                                    () -> sendErrorUnchecked(exchange, 404, "Message not found"));
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    if (messageService.delete(id)) {
                        sendNoContent(exchange, 204);
                    } else {
                        sendError(exchange, 404, "Message not found");
                    }
                } else {
                    exchange.getResponseHeaders().set("Allow", "GET, DELETE");
                    sendError(exchange, 405, "Method not allowed");
                }
                return;
            }

            sendError(exchange, 404, "Path not found");
        } catch (JsonProcessingException | IllegalArgumentException exception) {
            sendError(exchange, 400, "Invalid request");
        } catch (Exception exception) {
            sendError(exchange, 500, "Internal server error");
        }
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        CreateMessageRequest request;
        try (InputStream body = exchange.getRequestBody()) {
            request = objectMapper.readValue(body, CreateMessageRequest.class);
        }
        sendJson(exchange, 201, messageService.create(request));
    }

    private void sendJsonUnchecked(HttpExchange exchange, int status, Object value) {
        try {
            sendJson(exchange, status, value);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void sendErrorUnchecked(HttpExchange exchange, int status, String message) {
        try {
            sendError(exchange, status, message);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void sendJson(HttpExchange exchange, int status, Object value) throws IOException {
        byte[] response = objectMapper.writeValueAsBytes(value);
        exchange.getResponseHeaders().set("Content-Type", JSON_CONTENT_TYPE);
        exchange.getResponseHeaders().set("Content-Length", Long.toString(response.length));
        try {
            exchange.sendResponseHeaders(status, response.length);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        } finally {
            exchange.close();
        }
    }

    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        sendJson(exchange, status, new ErrorResponse(message));
    }

    private void sendNoContent(HttpExchange exchange, int status) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", JSON_CONTENT_TYPE);
        exchange.getResponseHeaders().set("Content-Length", "0");
        try {
            exchange.sendResponseHeaders(status, -1);
        } finally {
            exchange.close();
        }
    }

    private UUID parseMessageId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Invalid message id");
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid message id", exception);
        }
    }

    private Optional<String> parseAuthorQuery(URI uri) {
        String rawQuery = uri.getRawQuery();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return Optional.empty();
        }
        for (String parameter : rawQuery.split("&")) {
            int separator = parameter.indexOf('=');
            String rawName = separator >= 0 ? parameter.substring(0, separator) : parameter;
            if ("author".equals(URLDecoder.decode(rawName, StandardCharsets.UTF_8))) {
                String rawValue = separator >= 0 ? parameter.substring(separator + 1) : "";
                return Optional.of(URLDecoder.decode(rawValue, StandardCharsets.UTF_8));
            }
        }
        return Optional.empty();
    }
}
