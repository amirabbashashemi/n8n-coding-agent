package com.example.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A small HTTP server exposing an in-memory message collection.
 */
public final class SimpleApiServer {
    private static final int PORT = 9000;
    private static final String MESSAGE_PATH = "/api/messages";
    private static final String ALLOW_HEADER_VALUE = "GET, POST, DELETE";

    private final CopyOnWriteArrayList<String> messages = new CopyOnWriteArrayList<>();

    private SimpleApiServer() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        SimpleApiServer application = new SimpleApiServer();
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext(MESSAGE_PATH, application.new MessagesHandler());

        ExecutorService executor = Executors.newFixedThreadPool(10);
        server.setExecutor(executor);
        server.start();

        try {
            new CountDownLatch(1).await();
        } finally {
            server.stop(0);
            executor.shutdown();
        }
    }

    private final class MessagesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            boolean responseStarted = false;
            try (InputStream requestBody = exchange.getRequestBody()) {
                if (!MESSAGE_PATH.equals(exchange.getRequestURI().getPath())) {
                    send(exchange, 404, new byte[0], null);
                    responseStarted = true;
                    return;
                }

                String method = exchange.getRequestMethod();
                switch (method) {
                    case "GET" -> {
                        String response = String.join("\n", List.copyOf(messages));
                        send(exchange, 200, response.getBytes(StandardCharsets.UTF_8),
                                "text/plain; charset=UTF-8");
                        responseStarted = true;
                    }
                    case "POST" -> {
                        byte[] requestBytes = requestBody.readAllBytes();
                        if (requestBytes.length == 0) {
                            send(exchange, 400, new byte[0], null);
                        } else {
                            messages.add(new String(requestBytes, StandardCharsets.UTF_8));
                            send(exchange, 201, new byte[0], null);
                        }
                        responseStarted = true;
                    }
                    case "DELETE" -> {
                        messages.clear();
                        send(exchange, 204, new byte[0], null);
                        responseStarted = true;
                    }
                    default -> {
                        exchange.getResponseHeaders().set("Allow", ALLOW_HEADER_VALUE);
                        send(exchange, 405, new byte[0], null);
                        responseStarted = true;
                    }
                }
            } catch (Exception exception) {
                if (!responseStarted) {
                    try {
                        send(exchange, 500, new byte[0], null);
                    } catch (IOException ignored) {
                        // The connection may already have been closed by the client.
                    }
                }
            }
        }
    }

    private static void send(HttpExchange exchange, int statusCode, byte[] body,
                             String contentType) throws IOException {
        if (contentType != null) {
            exchange.getResponseHeaders().set("Content-Type", contentType);
        }
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
