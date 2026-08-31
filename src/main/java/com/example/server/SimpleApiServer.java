package com.example.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A small in-memory HTTP server for creating, listing, and deleting messages.
 */
public class SimpleApiServer {
    public static final int PORT = 9000;
    public static final String API_PATH = "/api/messages";
    public static final java.nio.charset.Charset CHARSET = StandardCharsets.UTF_8;
    private static final int MAX_REQUEST_BODY_BYTES = 1024 * 1024;
    private static final String ALLOWED_METHODS = "GET, POST, DELETE";
    private static final Logger LOGGER = Logger.getLogger(SimpleApiServer.class.getName());

    private final HttpServer server;
    private final ExecutorService executor;
    private final CopyOnWriteArrayList<String> messages = new CopyOnWriteArrayList<>();
    private volatile boolean started;

    public SimpleApiServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext(API_PATH, new MessageHandler());
        executor = Executors.newFixedThreadPool(4);
        server.setExecutor(executor);
    }

    public synchronized void start() {
        if (!started) {
            server.start();
            started = true;
        }
    }

    public synchronized void stop() {
        if (started) {
            server.stop(0);
            started = false;
        }
        executor.shutdown();
    }

    public static void main(String[] args) throws IOException {
        SimpleApiServer apiServer = new SimpleApiServer();
        apiServer.start();
    }

    private final class MessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                switch (exchange.getRequestMethod()) {
                    case "POST" -> handlePost(exchange);
                    case "GET" -> handleGet(exchange);
                    case "DELETE" -> handleDelete(exchange);
                    default -> {
                        exchange.getResponseHeaders().set("Allow", ALLOWED_METHODS);
                        sendResponse(exchange, 405, new byte[0], null);
                    }
                }
            } catch (IOException | RuntimeException exception) {
                LOGGER.log(Level.WARNING, "Unable to process HTTP request", exception);
                try {
                    sendResponse(exchange, 500,
                            "Internal Server Error".getBytes(CHARSET), "text/plain; charset=UTF-8");
                } catch (IOException | RuntimeException responseException) {
                    LOGGER.log(Level.FINE, "Unable to send error response", responseException);
                }
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            String contentLengthHeader = exchange.getRequestHeaders().getFirst("Content-Length");
            if (contentLengthHeader != null) {
                final long contentLength;
                try {
                    contentLength = Long.parseLong(contentLengthHeader);
                } catch (NumberFormatException exception) {
                    sendResponse(exchange, 400, "Bad Request".getBytes(CHARSET),
                            "text/plain; charset=UTF-8");
                    return;
                }
                if (contentLength < 0) {
                    sendResponse(exchange, 400, "Bad Request".getBytes(CHARSET),
                            "text/plain; charset=UTF-8");
                    return;
                }
                if (contentLength > MAX_REQUEST_BODY_BYTES) {
                    sendResponse(exchange, 413, new byte[0], null);
                    return;
                }
            }

            byte[] body;
            try (InputStream input = exchange.getRequestBody()) {
                body = readRequestBody(input);
            }
            if (body == null) {
                sendResponse(exchange, 413, new byte[0], null);
                return;
            }

            String message = new String(body, CHARSET);
            if (message.length() == 0) {
                sendResponse(exchange, 400, "Bad Request".getBytes(CHARSET),
                        "text/plain; charset=UTF-8");
                return;
            }
            messages.add(message);
            sendResponse(exchange, 201, new byte[0], null);
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            List<String> snapshot = List.copyOf(messages);
            String response = String.join("\n", snapshot);
            sendResponse(exchange, 200, response.getBytes(CHARSET),
                    "text/plain; charset=UTF-8");
        }

        private void handleDelete(HttpExchange exchange) throws IOException {
            messages.clear();
            sendResponse(exchange, 204, new byte[0], null);
        }

        private byte[] readRequestBody(InputStream input) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int total = 0;
            int read;
            while ((read = input.read(chunk)) != -1) {
                if (read > MAX_REQUEST_BODY_BYTES - total) {
                    return null;
                }
                buffer.write(chunk, 0, read);
                total += read;
            }
            return buffer.toByteArray();
        }
    }

    private static void sendResponse(HttpExchange exchange, int status, byte[] body,
                                     String contentType) throws IOException {
        byte[] responseBody = body == null ? new byte[0] : body;
        Headers headers = exchange.getResponseHeaders();
        if (contentType != null) {
            headers.set("Content-Type", contentType);
        }
        if (status == 204) {
            exchange.sendResponseHeaders(status, -1);
            try (OutputStream output = exchange.getResponseBody()) {
                // A 204 response must not contain a response body.
            }
            return;
        }
        exchange.sendResponseHeaders(status, responseBody.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(responseBody);
        }
    }
}
