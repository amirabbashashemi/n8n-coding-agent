package com.example.server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/** Application entry point for the HTTP server. */
public final class Main {
    private static final int DEFAULT_PORT = 8080;

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        int port = resolvePort();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> {
            byte[] response = "OK".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0), "http-server-shutdown"));
        server.start();
        System.out.println("Server started on port " + port);
    }

    private static int resolvePort() {
        String configuredPort = System.getenv("PORT");
        if (configuredPort == null || configuredPort.isBlank()) {
            return DEFAULT_PORT;
        }
        try {
            int port = Integer.parseInt(configuredPort);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("PORT must be between 1 and 65535");
            }
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("PORT must be a valid number", exception);
        }
    }
}
