package com.example.server.http;

import com.example.server.model.CreateMessageRequest;
import com.example.server.model.ErrorResponse;
import com.example.server.model.Message;
import com.example.server.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/** HTTP handler for the /api/messages resource and its UUID sub-resources. */
public final class MessageHandler implements HttpHandler {
    private static final String COLLECTION_PATH = "/api/messages";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";

    private final MessageService messageService;
    private final ObjectMapper objectMapper;
    @SuppressWarnings("unused")
    private final JsonHttpResponder responder;

    public MessageHandler(MessageService messageService, JsonHttpResponder responder,
                          ObjectMapper objectMapper) {
        this.messageService = messageService;
        this.responder = responder;
        this.objectMapper = objectMapper;
    }

    public MessageHandler(MessageService messageService, JsonHttpResponder responder) {
        this(messageService, responder, new ObjectMapper());
    }

    public MessageHandler(MessageService messageService, ObjectMapper objectMapper) {
        this(messageService, null, objectMapper);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod().toUpperCase(java.util.Locale.ROOT);
            String path = normalizePath(exchange.getRequestURI().getPath());

            if (COLLECTION_PATH.equals(path)) {
                handleCollection(exchange, method);
                return;
            }

            if (path.startsWith(COLLECTION_PATH + "/")) {
                handleItem(exchange, method, path.substring(COLLECTION_PATH.length() + 1));
                return;
            }

            sendError(exchange, 404, "Resource not found");
        } catch (Exception exception) {
            if (exchange.getResponseCode() == -1) {
                sendError(exchange, 500, "Internal server error");
            }
        } finally {
            exchange.close();
        }
    }

    private void handleCollection(HttpExchange exchange, String method) throws IOException {
        switch (method) {
            case "GET" -> {
                String author = queryParameter(exchange.getRequestURI().getRawQuery(), "author");
                List<Message> messages = messageService.findAll();
                if (author != null) {
                    String wantedAuthor = author.trim();
                    messages = messages.stream()
                            .filter(message -> message.author().equals(wantedAuthor))
                            .collect(Collectors.toList());
                }
                sendJson(exchange, 200, messages);
            }
            case "POST" -> createMessage(exchange);
            default -> methodNotAllowed(exchange, "GET, POST");
        }
    }

    private void createMessage(HttpExchange exchange) throws IOException {
        final CreateMessageRequest request;
        try (InputStream body = exchange.getRequestBody()) {
            request = objectMapper.readValue(body, CreateMessageRequest.class);
        } catch (Exception exception) {
            sendError(exchange, 400, "Invalid JSON request body");
            return;
        }

        String author = trimRequired(request.author(), "author");
        if (author == null) {
            sendError(exchange, 400, "author must not be blank");
            return;
        }
        String content = trimRequired(request.content(), "content");
        if (content == null) {
            sendError(exchange, 400, "content must not be blank");
            return;
        }
        String category = trimRequired(request.category(), "category");
        if (category == null) {
            sendError(exchange, 400, "category must not be blank");
            return;
        }

        Message message = messageService.create(author, content, category);
        sendJson(exchange, 201, message);
    }

    private void handleItem(HttpExchange exchange, String method, String rawId) throws IOException {
        if (rawId.isBlank() || rawId.contains("/")) {
            sendError(exchange, 400, "Invalid message UUID");
            return;
        }

        final UUID id;
        try {
            id = UUID.fromString(rawId);
        } catch (IllegalArgumentException exception) {
            sendError(exchange, 400, "Invalid message UUID");
            return;
        }

        switch (method) {
            case "GET" -> {
                Optional<Message> message = messageService.findById(id);
                if (message.isEmpty()) {
                    sendError(exchange, 404, "Message not found");
                } else {
                    sendJson(exchange, 200, message.get());
                }
            }
            case "DELETE" -> {
                if (!messageService.deleteById(id)) {
                    sendError(exchange, 404, "Message not found");
                } else {
                    exchange.sendResponseHeaders(204, -1);
                }
            }
            default -> methodNotAllowed(exchange, "GET, DELETE");
        }
    }

    private void sendJson(HttpExchange exchange, int status, Object payload) throws IOException {
        byte[] bytes = objectMapper.writeValueAsBytes(payload);
        exchange.getResponseHeaders().set("Content-Type", JSON_CONTENT_TYPE);
        exchange.getResponseHeaders().set("Content-Length", Integer.toString(bytes.length));
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        sendJson(exchange, status, new ErrorResponse(message));
    }

    private void methodNotAllowed(HttpExchange exchange, String allowed) throws IOException {
        exchange.getResponseHeaders().set("Allow", allowed);
        sendError(exchange, 405, "Method not allowed");
    }

    private static String normalizePath(String path) {
        if (path.length() > COLLECTION_PATH.length() && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    private static String trimRequired(String value, String ignoredName) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static String queryParameter(String query, String requestedName) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return Arrays.stream(query.split("&"))
                .map(pair -> pair.split("=", 2))
                .filter(pair -> pair.length == 2 && requestedName.equals(decode(pair[0])))
                .map(pair -> decode(pair[1]))
                .findFirst()
                .orElse(null);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
