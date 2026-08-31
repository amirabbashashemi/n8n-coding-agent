package com.example.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** HTTP routing and serialization for the message API. */
public final class MessageHandler implements HttpHandler {
    private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
    private static final String MESSAGES_PATH = "/api/messages";
    private static final String HEALTH_PATH = "/api/health";

    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    public MessageHandler(ObjectMapper objectMapper, MessageService messageService) {
        this.objectMapper = objectMapper;
        this.messageService = messageService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            List<String> path = parsePath(exchange);
            String method = exchange.getRequestMethod();

            if (HEALTH_PATH.equals(exchange.getRequestURI().getPath())) {
                if (!"GET".equals(method)) {
                    exchange.getResponseHeaders().set("Allow", "GET");
                    writeError(exchange, 405, "Method Not Allowed");
                } else {
                    Map<String, String> health = Map.of("status", "UP");
                    writeJson(exchange, 200, health);
                }
                return;
            }

            if (path.size() < 2 || !"api".equals(path.get(0)) || !"messages".equals(path.get(1))) {
                writeError(exchange, 404, "Not Found");
                return;
            }

            if (path.size() == 2) {
                handleCollection(exchange, method);
            } else if (path.size() == 3 && !path.get(2).isEmpty()) {
                handleItem(exchange, method, path.get(2));
            } else {
                writeError(exchange, 404, "Not Found");
            }
        } catch (IllegalArgumentException | JsonProcessingException e) {
            writeError(exchange, 400, "Invalid request");
        } catch (Exception e) {
            writeError(exchange, 500, "Internal Server Error");
        } finally {
            exchange.close();
        }
    }

    private void handleCollection(HttpExchange exchange, String method) throws IOException {
        switch (method) {
            case "GET" -> {
                Map<String, String> query = parseQuery(exchange);
                String author = query.get("author");
                writeJson(exchange, 200, messageService.getMessages(author));
            }
            case "POST" -> {
                CreateMessageRequest request = readJson(exchange, CreateMessageRequest.class);
                if (request == null || request.author() == null || request.content() == null
                        || request.category() == null) {
                    throw new IllegalArgumentException("Incomplete request body");
                }
                Message message = messageService.createMessage(request);
                writeJson(exchange, 201, message);
            }
            default -> {
                exchange.getResponseHeaders().set("Allow", "GET, POST");
                writeError(exchange, 405, "Method Not Allowed");
            }
        }
    }

    private void handleItem(HttpExchange exchange, String method, String idText) throws IOException {
        final UUID id;
        try {
            id = UUID.fromString(idText);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid message id", e);
        }

        switch (method) {
            case "GET" -> messageService.getMessage(id)
                    .ifPresentOrElse(message -> writeUnchecked(exchange, 200, message),
                            () -> writeUncheckedError(exchange, 404, "Message not found"));
            case "DELETE" -> {
                if (messageService.deleteMessage(id)) {
                    writeEmpty(exchange, 204);
                } else {
                    writeError(exchange, 404, "Message not found");
                }
            }
            default -> {
                exchange.getResponseHeaders().set("Allow", "GET, DELETE");
                writeError(exchange, 405, "Method Not Allowed");
            }
        }
    }

    private List<String> parsePath(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return Collections.emptyList();
        }
        String value = path.startsWith("/") ? path.substring(1) : path;
        List<String> segments = new ArrayList<>();
        for (String segment : value.split("/", -1)) {
            segments.add(segment);
        }
        return segments;
    }

    private Map<String, String> parseQuery(HttpExchange exchange) {
        String rawQuery = exchange.getRequestURI().getRawQuery();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (String pair : rawQuery.split("&", -1)) {
            if (pair.isEmpty()) {
                continue;
            }
            int separator = pair.indexOf('=');
            String rawKey = separator < 0 ? pair : pair.substring(0, separator);
            String rawValue = separator < 0 ? "" : pair.substring(separator + 1);
            String key = URLDecoder.decode(rawKey, StandardCharsets.UTF_8);
            String value = URLDecoder.decode(rawValue, StandardCharsets.UTF_8);
            result.put(key, value);
        }
        return result;
    }

    private <T> T readJson(HttpExchange exchange, Class<T> type) throws IOException {
        try (InputStream body = exchange.getRequestBody()) {
            JsonNode node = objectMapper.readTree(body);
            if (node == null || !node.isObject()) {
                throw new JsonProcessingException("Request body must be a JSON object") { };
            }
            return objectMapper.treeToValue(node, type);
        }
    }

    private void writeJson(HttpExchange exchange, int status, Object value) throws IOException {
        byte[] bytes = objectMapper.writeValueAsString(value).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", JSON_CONTENT_TYPE);
        exchange.getResponseHeaders().set("Content-Length", Integer.toString(bytes.length));
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private void writeEmpty(HttpExchange exchange, int status) throws IOException {
        exchange.getResponseHeaders().set("Content-Length", "0");
        exchange.sendResponseHeaders(status, 0);
        exchange.getResponseBody().close();
    }

    private void writeError(HttpExchange exchange, int status, String message) throws IOException {
        writeJson(exchange, status, new ErrorResponse(message));
    }

    private void writeUnchecked(HttpExchange exchange, int status, Object value) {
        try {
            writeJson(exchange, status, value);
        } catch (IOException e) {
            throw new ResponseWriteException(e);
        }
    }

    private void writeUncheckedError(HttpExchange exchange, int status, String message) {
        try {
            writeError(exchange, status, message);
        } catch (IOException e) {
            throw new ResponseWriteException(e);
        }
    }

    private static final class ResponseWriteException extends RuntimeException {
        private ResponseWriteException(IOException cause) {
            super(cause);
        }
    }
}
