package com.example.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

/** Application entry point for the lightweight message HTTP service. */
public final class Main {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final List<Map<String, Object>> MESSAGES = new CopyOnWriteArrayList<>();

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        int port = readPort();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", Main::handleHealth);
        server.createContext("/messages", Main::handleMessages);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
        System.out.printf("Message server listening on port %d%n", port);
    }

    private static int readPort() {
        String configuredPort = System.getenv().getOrDefault("PORT", "8080");
        try {
            int port = Integer.parseInt(configuredPort);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException("outside valid range");
            }
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("PORT must be an integer from 1 to 65535", exception);
        }
    }

    private static void handleHealth(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, Map.of("error", "method not allowed"));
            return;
        }
        send(exchange, 200, Map.of("status", "UP"));
    }

    private static void handleMessages(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            send(exchange, 200, new ArrayList<>(MESSAGES));
            return;
        }
        if ("POST".equalsIgnoreCase(method)) {
            createMessage(exchange);
            return;
        }
        send(exchange, 405, Map.of("error", "method not allowed"));
    }

    private static void createMessage(HttpExchange exchange) throws IOException {
        try (InputStream body = exchange.getRequestBody()) {
            Map<String, Object> payload = OBJECT_MAPPER.readValue(body,
                    new TypeReference<LinkedHashMap<String, Object>>() { });
            Object content = payload.get("content");
            if (!(content instanceof String text) || text.isBlank()) {
                send(exchange, 400, Map.of("error", "content is required"));
                return;
            }
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("id", MESSAGES.size() + 1L);
            message.put("content", text);
            MESSAGES.add(Collections.unmodifiableMap(message));
            send(exchange, 201, message);
        } catch (Exception exception) {
            send(exchange, 400, Map.of("error", "request body must be valid JSON"));
        }
    }

    private static void send(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] response = OBJECT_MAPPER.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, response.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }
}
