package com.example.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

/**
 * A small in-memory HTTP server for storing and retrieving text messages.
 */
public class SimpleApiServer {
    private static final int PORT = 9000;
    private static final String MESSAGE_PATH = "/api/messages";
    private static final String ALLOWED_METHODS = "GET, POST";

    private final CopyOnWriteArrayList<String> messages = new CopyOnWriteArrayList<>();
    private final HttpServer server;

    /**
     * Creates a server bound to the configured port.
     *
     * @throws IOException if the listening socket cannot be created
     */
    public SimpleApiServer() throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
    }

    /**
     * Registers the messages endpoint and starts accepting requests.
     *
     * @throws IOException if the endpoint cannot be registered or the server cannot start
     */
    public void start() throws IOException {
        server.createContext(MESSAGE_PATH, this::handleMessages);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
        System.out.println("Simple API server started on port " + PORT);
    }

    private void handleMessages(HttpExchange exchange) {
        boolean headersSent = false;
        try {
            String method = exchange.getRequestMethod();
            if ("POST".equalsIgnoreCase(method)) {
                String message;
                try (InputStream requestBody = exchange.getRequestBody()) {
                    message = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
                }
                messages.add(message);
                headersSent = true;
                sendEmptyResponse(exchange, 201);
            } else if ("GET".equalsIgnoreCase(method)) {
                List<String> snapshot = new ArrayList<>(messages);
                byte[] response = String.join("\n", snapshot).getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                headersSent = true;
                sendResponse(exchange, 200, response);
            } else {
                exchange.getResponseHeaders().set("Allow", ALLOWED_METHODS);
                headersSent = true;
                sendEmptyResponse(exchange, 405);
            }
        } catch (Exception exception) {
            System.err.println("Error while handling HTTP request: " + exception.getMessage());
            if (!headersSent) {
                try {
                    byte[] response = "Internal Server Error".getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                    sendResponse(exchange, 500, response);
                } catch (IOException | RuntimeException errorResponseFailure) {
                    System.err.println("Unable to send error response: " + errorResponseFailure.getMessage());
                }
            }
        } finally {
            exchange.close();
        }
    }

    private static void sendEmptyResponse(HttpExchange exchange, int statusCode) throws IOException {
        exchange.sendResponseHeaders(statusCode, -1);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            // The response intentionally has no body.
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, byte[] response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(response);
        }
    }

    public static void main(String[] args) {
        try {
            new SimpleApiServer().start();
        } catch (IOException exception) {
            System.err.println("Failed to start Simple API server: " + exception.getMessage());
            System.exit(1);
        }
    }
}
