package com.example.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimpleApiServer {
    private static final List<String> messages = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/messages", new MessageHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8080");
    }

    static class MessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                handlePost(exchange);
            } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                handleGet(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            String body;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
                body = reader.readLine();
            }
            messages.add(body);
            exchange.sendResponseHeaders(201, -1);
            exchange.close();
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            String response = String.join("\n", messages);
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}