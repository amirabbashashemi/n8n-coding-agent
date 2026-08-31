package com.example.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpContext;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleApiServer {

    private static final int PORT = 9000;
    private static final ConcurrentHashMap<String, String> messages = new ConcurrentHashMap<>();

    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        HttpContext postContext = server.createContext("/api/messages", createPostHandler());
        HttpContext deleteContext = server.createContext("/api/messages", createDeleteHandler());

        server.setExecutor(null); // creates a default executor
        server.start();
    }

    private static HttpHandler createPostHandler() {
        return new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("POST" .equals(exchange.getRequestMethod())) {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    if (requestBody.isEmpty()) {
                        exchange.sendResponseHeaders(400, -1); // Bad Request
                        return;
                    }
                    messages.put(String.valueOf(messages.size() + 1), requestBody);
                    exchange.sendResponseHeaders(200, -1); // OK
                }
            }
        };
    }

    private static HttpHandler createDeleteHandler() {
        return new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("DELETE".equals(exchange.getRequestMethod())) {
                    messages.clear();
                    exchange.sendResponseHeaders(204, -1); // No Content
                }
            }
        };
    }

    public static void main(String[] args) throws IOException {
        startServer();
        System.out.println("Server started on port " + PORT);
    }
}