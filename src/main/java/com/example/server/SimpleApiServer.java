package com.example.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

/**
 * A small in-memory HTTP service for managing plain-text messages.
 */
public class SimpleApiServer {
    private static final int PORT = 9000;
    private static final String MESSAGES_PATH = "/api/messages";
    private static final List<String> MESSAGES = new CopyOnWriteArrayList<>();

    private SimpleApiServer() {
        // Utility class; do not instantiate.
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext(MESSAGES_PATH, new MessagesHandler());
        server.setExecutor(Executors.newFixedThreadPool(
                Math.max(2, Runtime.getRuntime().availableProcessors())));
        server.start();
        System.out.println("Server started on port " + PORT + ".");
    }

    private static final class MessagesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (!MESSAGES_PATH.equals(exchange.getRequestURI().getPath())) {
                    send(exchange, 404, "Not Found");
                    return;
                }

                switch (exchange.getRequestMethod()) {
                    case "GET" -> handleGet(exchange);
                    case "POST" -> handlePost(exchange);
                    case "DELETE" -> handleDelete(exchange);
                    default -> {
                        exchange.getResponseHeaders().set("Allow", "GET, POST, DELETE");
                        send(exchange, 405, "Method Not Allowed");
                    }
                }
            } finally {
                exchange.close();
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            String response = String.join("\n", MESSAGES);
            send(exchange, 200, response);
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            byte[] body;
            try (InputStream requestBody = exchange.getRequestBody()) {
                body = requestBody.readAllBytes();
            }

            String message = new String(body, StandardCharsets.UTF_8);
            if (message.isBlank()) {
                send(exchange, 400, "Bad Request");
                return;
            }

            MESSAGES.add(message);
            send(exchange, 201, "");
        }

        private void handleDelete(HttpExchange exchange) throws IOException {
            MESSAGES.clear();
            exchange.sendResponseHeaders(204, -1);
        }

        private static void send(HttpExchange exchange, int status, String response)
                throws IOException {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(status, bytes.length);
            try (var responseBody = exchange.getResponseBody()) {
                responseBody.write(bytes);
            }
        }
    }
}
