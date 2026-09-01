package com.example.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A small in-memory HTTP server for managing plain-text messages.
 */
public class SimpleApiServer {
    public static final int PORT = 9000;
    public static final String API_PATH = "/api/messages";

    private final CopyOnWriteArrayList<String> messages = new CopyOnWriteArrayList<>();
    private HttpServer server;
    private ExecutorService executor;

    public static void main(String[] args) throws IOException {
        SimpleApiServer simpleApiServer = new SimpleApiServer();
        simpleApiServer.startServer();
    }

    /**
     * Starts the server if it is not already running.
     *
     * @throws IOException if the listening socket cannot be created
     */
    public synchronized void startServer() throws IOException {
        if (server != null) {
            return;
        }

        HttpServer newServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        ExecutorService newExecutor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            newServer.createContext(API_PATH, this::handleRequest);
            newServer.setExecutor(newExecutor);
            newServer.start();
            server = newServer;
            executor = newExecutor;
        } catch (RuntimeException | IOException exception) {
            newExecutor.shutdownNow();
            newServer.stop(0);
            throw exception;
        }
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        boolean responseStarted = false;
        try {
            if (!API_PATH.equals(exchange.getRequestURI().getPath())) {
                sendTextResponse(exchange, 404, "Not Found");
                return;
            }

            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET" -> {
                    String responseBody = String.join("\n", messages);
                    sendTextResponse(exchange, 200, responseBody);
                }
                case "POST" -> {
                    String message;
                    try (var requestBody = exchange.getRequestBody()) {
                        message = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
                    }
                    if (message.isEmpty()) {
                        sendTextResponse(exchange, 400, "Request body must not be empty");
                    } else {
                        messages.add(message);
                        sendTextResponse(exchange, 201, "");
                    }
                }
                case "DELETE" -> {
                    messages.clear();
                    sendNoContentResponse(exchange);
                }
                default -> {
                    exchange.getResponseHeaders().set("Allow", "GET, POST, DELETE");
                    sendTextResponse(exchange, 405, "Method Not Allowed");
                }
            }
        } catch (IOException | RuntimeException exception) {
            if (!responseStarted) {
                try {
                    sendTextResponse(exchange, 500, "Internal Server Error");
                } catch (IOException ignored) {
                    // The connection may already have been closed or committed.
                }
            }
        } finally {
            exchange.close();
        }
    }

    private void sendTextResponse(HttpExchange exchange, int statusCode, String body)
            throws IOException {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bodyBytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bodyBytes);
        }
    }

    private void sendNoContentResponse(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(204, -1);
    }

    /**
     * Stops the server and releases its request executor.
     */
    public synchronized void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }
}
