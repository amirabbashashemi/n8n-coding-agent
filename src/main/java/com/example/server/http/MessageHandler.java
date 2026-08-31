package com.example.server.http;

import com.example.server.model.ApiError;
import com.example.server.model.CreateMessageRequest;
import com.example.server.model.Message;
import com.example.server.service.MessageService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** HTTP endpoint for the message collection and individual messages. */
public final class MessageHandler implements HttpHandler {
    private static final String COLLECTION_PATH = "/api/messages";
    private static final int MAX_BODY_BYTES = 1_048_576;

    private final MessageService messageService;
    private final JsonSupport jsonSupport;

    public MessageHandler(MessageService messageService, JsonSupport jsonSupport) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.jsonSupport = Objects.requireNonNull(jsonSupport, "jsonSupport");
    }

    @Override
    public void handle(HttpExchange exchange) {
        try (exchange) {
            try {
                Route route = route(exchange.getRequestURI().getPath());
                if (route == null) {
                    sendError(exchange, 400, "BAD_REQUEST", "Invalid message path");
                    return;
                }

                String method = exchange.getRequestMethod();
                if ("POST".equalsIgnoreCase(method)) {
                    if (!route.collection()) {
                        sendError(exchange, 405, "METHOD_NOT_ALLOWED", "POST is only supported on the message collection", "GET, DELETE");
                        return;
                    }
                    create(exchange);
                } else if ("GET".equalsIgnoreCase(method)) {
                    get(exchange, route);
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    if (route.collection()) {
                        sendError(exchange, 405, "METHOD_NOT_ALLOWED", "DELETE requires a message id", "GET, POST");
                    } else {
                        delete(exchange, route.id());
                    }
                } else {
                    sendError(exchange, 405, "METHOD_NOT_ALLOWED", "Method not allowed", route.collection() ? "GET, POST" : "GET, DELETE");
                }
            } catch (BadRequestException e) {
                sendError(exchange, 400, "BAD_REQUEST", e.getMessage());
            } catch (NotFoundException e) {
                sendError(exchange, 404, "NOT_FOUND", e.getMessage());
            } catch (Exception e) {
                sendError(exchange, 500, "INTERNAL_SERVER_ERROR", "An unexpected error occurred");
            }
        } catch (IOException ignored) {
            // The client may have disconnected while the response was being closed.
        }
    }

    private void create(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        final CreateMessageRequest request;
        try {
            request = jsonSupport.fromJson(body, CreateMessageRequest.class);
        } catch (RuntimeException e) {
            throw new BadRequestException("Request body is not valid JSON");
        }
        if (request == null || blank(request.author()) || blank(request.content()) || blank(request.category())) {
            throw new BadRequestException("author, content and category must not be blank");
        }
        Message message;
        try {
            message = messageService.create(request);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage() == null ? "Invalid message" : e.getMessage());
        }
        sendJson(exchange, 201, message);
    }

    private void get(HttpExchange exchange, Route route) throws IOException {
        if (route.collection()) {
            String author = queryAuthor(exchange.getRequestURI().getRawQuery());
            List<Message> messages = author == null
                    ? messageService.findAll()
                    : messageService.findByAuthor(author);
            sendJson(exchange, 200, messages);
            return;
        }
        Message message = messageService.findById(route.id()).orElseThrow(
                () -> new NotFoundException("Message not found"));
        sendJson(exchange, 200, message);
    }

    private void delete(HttpExchange exchange, UUID id) throws IOException {
        if (!messageService.deleteById(id)) {
            throw new NotFoundException("Message not found");
        }
        exchange.sendResponseHeaders(204, -1);
    }

    private String readBody(HttpExchange exchange) throws IOException {
        long declared = exchange.getRequestHeaders().getFirst("Content-Length") == null
                ? -1 : parseLength(exchange.getRequestHeaders().getFirst("Content-Length"));
        if (declared > MAX_BODY_BYTES) {
            throw new BadRequestException("Request body is too large");
        }
        try (var input = exchange.getRequestBody(); var output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int total = 0;
            int read;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > MAX_BODY_BYTES) {
                    throw new BadRequestException("Request body is too large");
                }
                output.write(buffer, 0, read);
            }
            return output.toString(StandardCharsets.UTF_8);
        }
    }

    private static long parseLength(String value) {
        try {
            long length = Long.parseLong(value);
            if (length < 0) throw new NumberFormatException();
            return length;
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid Content-Length");
        }
    }

    private String queryAuthor(String query) {
        if (query == null || query.isEmpty()) return null;
        String author = null;
        for (String part : query.split("&", -1)) {
            String[] pair = part.split("=", 2);
            if (pair.length != 2 || !"author".equals(pair[0]) || pair[1].isEmpty()) {
                throw new BadRequestException("Invalid query parameter");
            }
            try {
                author = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid query parameter");
            }
            if (blank(author)) throw new BadRequestException("author must not be blank");
        }
        return author;
    }

    private Route route(String path) {
        if (COLLECTION_PATH.equals(path)) return new Route(true, null);
        if (!path.startsWith(COLLECTION_PATH + "/")) return null;
        String value = path.substring(COLLECTION_PATH.length() + 1);
        if (value.isEmpty() || value.indexOf('/') >= 0) return null;
        try {
            return new Route(false, UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid message id");
        }
    }

    private void sendJson(HttpExchange exchange, int status, Object value) throws IOException {
        byte[] bytes;
        try {
            bytes = jsonSupport.toJson(value).getBytes(StandardCharsets.UTF_8);
        } catch (RuntimeException e) {
            throw new IOException("Unable to serialize response", e);
        }
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int status, String code, String message) throws IOException {
        sendError(exchange, status, code, message, null);
    }

    private void sendError(HttpExchange exchange, int status, String code, String message, String allow) throws IOException {
        if (allow != null) exchange.getResponseHeaders().set("Allow", allow);
        sendJson(exchange, status, new ApiError(code, message));
    }

    private static boolean blank(String value) { return value == null || value.trim().isEmpty(); }

    private record Route(boolean collection, UUID id) {}
    private static final class BadRequestException extends RuntimeException {
        BadRequestException(String message) { super(message); }
    }
    private static final class NotFoundException extends RuntimeException {
        NotFoundException(String message) { super(message); }
    }
}
