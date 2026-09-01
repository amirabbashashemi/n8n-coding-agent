package com.example.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleApiServer {
    private static final int PORT = 9000;
    private static final String API_PATH = "/api/messages";
    private static final String ALLOWED_METHODS = "GET, POST, DELETE";

    private final CopyOnWriteArrayList<String> messages = new CopyOnWriteArrayList<>();
    private HttpServer server;
    private ExecutorService executor;

    public static void main(String[] args) throws IOException {
        SimpleApiServer apiServer = new SimpleApiServer();
        apiServer.startServer();
    }

    public synchronized void startServer() throws IOException {
        if (server != null) {
            return;
        }

        HttpServer newServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        ExecutorService newExecutor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            newServer.createContext(API_PATH, new MessageHandler());
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

    private final class MessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            ResponseState responseState = new ResponseState();
            try {
                if (!API_PATH.equals(exchange.getRequestURI().getPath())) {
                    sendTextResponse(exchange, 404, "Not Found", responseState);
                    return;
                }

                switch (exchange.getRequestMethod()) {
                    case "GET" -> handleGet(exchange, responseState);
                    case "POST" -> handlePost(exchange, responseState);
                    case "DELETE" -> handleDelete(exchange, responseState);
                    default -> {
                        exchange.getResponseHeaders().set("Allow", ALLOWED_METHODS);
                        sendTextResponse(exchange, 405, "Method Not Allowed", responseState);
                    }
                }
            } catch (IOException | RuntimeException exception) {
                if (!responseState.headersSent) {
                    try {
                        sendTextResponse(exchange, 500, "Internal Server Error", responseState);
                    } catch (IOException | RuntimeException ignored) {
                        // The exchange may already be closed or unusable.
                    }
                }
            } finally {
                exchange.close();
            }
        }

        private void handleGet(HttpExchange exchange, ResponseState responseState) throws IOException {
            String body = String.join("\n", messages);
            sendTextResponse(exchange, 200, body, responseState);
        }

        private void handlePost(HttpExchange exchange, ResponseState responseState) throws IOException {
            String body;
            try (InputStream input = exchange.getRequestBody()) {
                body = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }

            if (body.isEmpty()) {
                sendTextResponse(exchange, 400, "Bad Request", responseState);
                return;
            }

            messages.add(body);
            sendTextResponse(exchange, 201, "", responseState);
        }

        private void handleDelete(HttpExchange exchange, ResponseState responseState) throws IOException {
            messages.clear();
            sendTextResponse(exchange, 204, "", responseState);
        }
    }

    private static void sendTextResponse(
            HttpExchange exchange,
            int statusCode,
            String body,
            ResponseState responseState) throws IOException {
        byte[] responseBody = statusCode == 204
                ? new byte[0]
                : body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBody.length);
        responseState.headersSent = true;
        try (OutputStream output = exchange.getResponseBody()) {
            if (responseBody.length > 0) {
                output.write(responseBody);
            }
        }
    }

    private static final class ResponseState {
        private boolean headersSent;
    }
}
