package com.example.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleApiServer {
    private static final MessageStore messageStore = new MessageStore();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(9000), 0);
        server.createContext("/api/messages", new MessageHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 9000");
    }

    static class MessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                handlePost(exchange);
            } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                handleGet(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            String message = new BufferedReader(new InputStreamReader(is)).lines().collect(java.util.stream.Collectors.joining());
            messageStore.addMessage(message);
            exchange.sendResponseHeaders(201, -1); // Created
            exchange.getResponseBody().close();
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            String response = messageStore.getMessages();
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}