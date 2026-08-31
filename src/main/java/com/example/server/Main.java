package com.example.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Application entry point for the HTTP service.
 */
public final class Main {
    private static final int DEFAULT_PORT = 8080;

    private Main() {
        // Utility class.
    }

    public static void main(String[] args) throws IOException {
        int port = configuredPort();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", Main::health);
        server.createContext("/", Main::notFound);
        server.setExecutor(null);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "http-server-shutdown"));
        server.start();
    }

    private static int configuredPort() {
        String value = System.getProperty("server.port", System.getenv("PORT"));
        if (value == null || value.isBlank()) {
            return DEFAULT_PORT;
        }
        try {
            int port = Integer.parseInt(value);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("server.port must be between 1 and 65535");
            }
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("server.port must be a valid integer", exception);
        }
    }

    private static void health(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Allow", "GET");
            respond(exchange, 405, "{\"error\":\"method_not_allowed\"}");
            return;
        }
        respond(exchange, 200, "{\"status\":\"ok\"}");
    }

    private static void notFound(HttpExchange exchange) throws IOException {
        respond(exchange, 404, "{\"error\":\"not_found\"}");
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Content-Length", Integer.toString(bytes.length));
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
