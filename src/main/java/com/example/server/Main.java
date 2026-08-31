package com.example.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/** Application entry point for the embedded HTTP server. */
public final class Main {
    private static final int DEFAULT_PORT = 8080;

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        int port = configuredPort();
        MessageHandler messageHandler = new MessageHandler();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/messages", new DelegatingHandler(messageHandler));
        server.setExecutor(null);
        server.start();
        System.out.println("Message server listening on port " + port);
    }

    private static int configuredPort() {
        String value = System.getenv("PORT");
        if (value == null || value.isBlank()) {
            return DEFAULT_PORT;
        }
        try {
            int port = Integer.parseInt(value);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException("port out of range");
            }
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("PORT must be a valid TCP port", exception);
        }
    }

    /** Bridges the domain handler to the JDK HTTP server without coupling its API to HttpHandler. */
    private static final class DelegatingHandler implements HttpHandler {
        private final MessageHandler delegate;
        private final Method requestMethod;

        private DelegatingHandler(MessageHandler delegate) {
            this.delegate = delegate;
            this.requestMethod = findRequestMethod(delegate.getClass());
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (requestMethod == null) {
                send(exchange, 501, "Message handler does not expose an HTTP request method");
                return;
            }
            try {
                requestMethod.invoke(delegate, exchange);
            } catch (IllegalAccessException exception) {
                send(exchange, 500, "Unable to access message handler");
            } catch (InvocationTargetException exception) {
                Throwable cause = exception.getCause();
                if (cause instanceof IOException ioException) {
                    throw ioException;
                }
                send(exchange, 500, "Message handler failed");
            }
        }

        private static Method findRequestMethod(Class<?> type) {
            for (Method method : type.getMethods()) {
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length == 1 && parameters[0] == HttpExchange.class) {
                    method.setAccessible(true);
                    return method;
                }
            }
            return null;
        }

        private static void send(HttpExchange exchange, int status, String body) throws IOException {
            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(status, payload.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(payload);
            }
        }
    }
}
