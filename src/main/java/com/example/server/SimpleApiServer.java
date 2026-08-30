package com.example.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.net.InetSocketAddress;

public class SimpleApiServer {
    private static List<String> messages = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(9090), 0);
        server.createContext("/api/v1/messages", new MessageHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server is running on port 9090");
    }

    static class MessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "";
            int statusCode;

            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream inputStream = exchange.getRequestBody();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder messageBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    messageBuilder.append(line);
                }
                messages.add(messageBuilder.toString());
                statusCode = 201; // Created
                response = "Message added";
            } else if ("GET".equals(exchange.getRequestMethod())) {
                response = String.join("\n", messages);
                statusCode = 200; // OK
            } else {
                statusCode = 405; // Method Not Allowed
                response = "Method Not Allowed";
            }

            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}