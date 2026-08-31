package com.example.server.handler;

import com.example.server.model.MessageStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessageHandler implements HttpHandler {
    private final MessageStore messageStore = new MessageStore();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response;
        if ("POST".equals(exchange.getRequestMethod())) {
            response = handlePostMessage(exchange);
        } else if ("GET".equals(exchange.getRequestMethod())) {
            response = handleGetMessages(exchange);
        } else {
            response = "Method Not Allowed";
            exchange.sendResponseHeaders(405, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }

        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private String handlePostMessage(HttpExchange exchange) throws IOException {
        String result;
        try (InputStream is = exchange.getRequestBody();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder message = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                message.append(line);
            }

            messageStore.addMessage(message.toString());
            result = "Message added";
        }
        return result;
    }

    private String handleGetMessages(HttpExchange exchange) {
        List<String> messages = messageStore.getMessages();
        return String.join("\n", messages);
    }
}