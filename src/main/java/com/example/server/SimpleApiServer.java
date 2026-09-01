package com.example.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A small in-memory HTTP server for managing text messages.
 */
public class SimpleApiServer {
    private static final int PORT = 9000;
    private static final String API_PATH = "/api/messages";
    private static final String ALLOWED_METHODS = "GET, POST, DELETE";

    private final CopyOnWriteArrayList<String> messages = new CopyOnWriteArrayList<>();
    private volatile HttpServer server;
    private volatile ExecutorService executor;

    public static void main(String[] args) throws IOException {
        SimpleApiServer apiServer = new SimpleApiServer();
        apiServer.startServer();
    }

    public synchronized void startServer() throws IOException {
        if (server != null) {
            return;
        }

        HttpServer createdServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        ExecutorService createdExecutor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            createdServer.createContext(API_PATH, this::handleApiRequest);
            createdServer.createContext("/", this::handleInvalidPath);
            createdServer.setExecutor(createdExecutor);
            createdServer.start();
            server = createdServer;
            executor = createdExecutor;
        } catch (RuntimeException | IOException failure) {
            createdServer.stop(0);
            createdExecutor.shutdownNow();
            throw failure;
        }
    }

    public synchronized void stopServer() {
        HttpServer activeServer = server;
        ExecutorService activeExecutor = executor;
        server = null;
        executor = null;

        if (activeServer != null) {
            activeServer.stop(0);
        }
        if (activeExecutor != null) {
            activeExecutor.shutdownNow();
        }
    }

    private void handleInvalidPath(HttpExchange exchange) {
        handleRequest(exchange, false);
    }

    private void handleApiRequest(HttpExchange exchange) {
        handleRequest(exchange, true);
    }

    private void handleRequest(HttpExchange exchange, boolean apiContext) {
        boolean responseStarted = false;
        try {
            if (!apiContext || !API_PATH.equals(exchange.getRequestURI().getPath())) {
                responseStarted = true;
                sendTextResponse(exchange, 404, "Not Found");
                return;
            }

            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET" -> {
                    String body = String.join("\n", new ArrayList<>(messages));
                    responseStarted = true;
                    sendTextResponse(exchange, 200, body);
                }
                case "POST" -> {
                    String message;
                    try (InputStream requestBody = exchange.getRequestBody()) {
                        message = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
                    }
                    if (message.isEmpty()) {
                        responseStarted = true;
                        sendTextResponse(exchange, 400, "Message body must not be empty");
                    } else {
                        messages.add(message);
                        responseStarted = true;
                        sendTextResponse(exchange, 201, "");
                    }
                }
                case "DELETE" -> {
                    messages.clear();
                    responseStarted = true;
                    sendEmptyResponse(exchange, 204);
                }
                default -> {
                    exchange.getResponseHeaders().set("Allow", ALLOWED_METHODS);
                    responseStarted = true;
                    sendTextResponse(exchange, 405, "Method Not Allowed");
                }
            }
        } catch (IOException | RuntimeException failure) {
            if (!responseStarted) {
                try {
                    sendTextResponse(exchange, 500, "Internal Server Error");
                } catch (IOException | RuntimeException ignored) {
                    // The connection may already have been closed by the client.
                }
            }
        } finally {
            exchange.close();
        }
    }

    private void sendTextResponse(HttpExchange exchange, int statusCode, String body)
            throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }

    private void sendEmptyResponse(HttpExchange exchange, int statusCode) throws IOException {
        exchange.sendResponseHeaders(statusCode, -1);
        try (OutputStream output = exchange.getResponseBody()) {
            output.flush();
        }
    }
}
