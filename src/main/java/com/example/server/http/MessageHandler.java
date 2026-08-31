package com.example.server.http;

import com.example.server.dto.CreateMessageRequest;
import com.example.server.dto.ErrorResponse;
import com.example.server.model.Message;
import com.example.server.service.MessageService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/** HTTP controller for the /api/messages resource. */
public final class MessageHandler implements HttpHandler {
    private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());
    private static final String BASE_PATH = "/api/messages";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

    private final MessageService messageService;
    private final JsonSupport jsonSupport;

    public MessageHandler(MessageService messageService, JsonSupport jsonSupport) {
        this.messageService = java.util.Objects.requireNonNull(messageService, "messageService");
        this.jsonSupport = java.util.Objects.requireNonNull(jsonSupport, "jsonSupport");
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            URI uri = exchange.getRequestURI();
            String path = uri.getPath();
            String method = exchange.getRequestMethod();

            if (path == null || !path.startsWith(BASE_PATH)) {
                sendError(exchange, 404, "Resource not found");
                return;
            }

            if (BASE_PATH.equals(path)) {
                if ("POST".equalsIgnoreCase(method)) {
                    handleCreate(exchange);
                } else if ("GET".equalsIgnoreCase(method)) {
                    handleList(exchange, uri.getRawQuery());
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
                return;
            }

            if (!path.startsWith(BASE_PATH + "/")
                    || path.length() == BASE_PATH.length() + 1
                    || path.substring(BASE_PATH.length() + 1).contains("/")) {
                sendError(exchange, 404, "Resource not found");
                return;
            }

            if (!"GET".equalsIgnoreCase(method) && !"DELETE".equalsIgnoreCase(method)) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            String idText = path.substring(BASE_PATH.length() + 1);
            UUID id;
            try {
                id = UUID.fromString(idText);
            } catch (IllegalArgumentException exception) {
                sendError(exchange, 400, "Invalid message id");
                return;
            }

            if (uri.getRawQuery() != null && !uri.getRawQuery().isEmpty()) {
                sendError(exchange, 400, "Query parameters are not allowed for a message id");
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                messageService.get(id)
                        .ifPresentOrElse(message -> sendJsonUnchecked(exchange, 200, message),
                                () -> sendErrorUnchecked(exchange, 404, "Message not found"));
            } else if (messageService.delete(id)) {
                sendNoContent(exchange);
            } else {
                sendError(exchange, 404, "Message not found");
            }
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "I/O error while handling message request", exception);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Unexpected error while handling message request", exception);
            try {
                sendError(exchange, 500, "Internal server error");
            } catch (IOException responseException) {
                LOGGER.log(Level.WARNING, "Unable to send internal error response", responseException);
            }
        }
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        try (InputStream body = exchange.getRequestBody()) {
            CreateMessageRequest request = jsonSupport.read(body, CreateMessageRequest.class);
            Message created = messageService.create(request);
            sendJson(exchange, 201, created);
        } catch (IllegalArgumentException exception) {
            sendError(exchange, 400, safeMessage(exception, "Invalid message"));
        } catch (IOException exception) {
            sendError(exchange, 400, "Invalid JSON request body");
        }
    }

    private void handleList(HttpExchange exchange, String rawQuery) throws IOException {
        String author = parseAuthor(rawQuery);
        List<Message> messages = messageService.list(author);
        sendJson(exchange, 200, messages);
    }

    private String parseAuthor(String rawQuery) {
        if (rawQuery == null || rawQuery.isEmpty()) {
            return null;
        }
        String found = null;
        for (String parameter : rawQuery.split("&", -1)) {
            if (parameter.isEmpty()) {
                throw new IllegalArgumentException("Invalid query string");
            }
            int separator = parameter.indexOf('=');
            if (separator <= 0 || separator != parameter.lastIndexOf('=')) {
                throw new IllegalArgumentException("Invalid query string");
            }
            String key = decode(parameter.substring(0, separator));
            if (!"author".equals(key) || found != null) {
                throw new IllegalArgumentException("Invalid query parameter");
            }
            found = decode(parameter.substring(separator + 1));
        }
        return found;
    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid URL encoding", exception);
        }
    }

    private void sendJson(HttpExchange exchange, int status, Object value) throws IOException {
        byte[] payload = jsonSupport.write(value).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", JSON_CONTENT_TYPE);
        exchange.getResponseHeaders().set("Content-Length", Integer.toString(payload.length));
        try (OutputStream response = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(status, payload.length);
            response.write(payload);
        }
    }

    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        sendJson(exchange, status, new ErrorResponse(message));
    }

    private void sendNoContent(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Length", "0");
        try (OutputStream response = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(204, 0);
        }
    }

    private void sendJsonUnchecked(HttpExchange exchange, int status, Object value) {
        try {
            sendJson(exchange, status, value);
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "I/O error while sending response", exception);
        }
    }

    private void sendErrorUnchecked(HttpExchange exchange, int status, String message) {
        try {
            sendError(exchange, status, message);
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "I/O error while sending error response", exception);
        }
    }

    private String safeMessage(IllegalArgumentException exception, String fallback) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? fallback : message;
    }
}
