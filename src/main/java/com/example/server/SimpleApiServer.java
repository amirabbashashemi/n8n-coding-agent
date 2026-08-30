package com.example.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SimpleApiServer {
    private static final int PORT = 8080;
    private static Map<String, String> dataStore = new HashMap<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new java.net.InetSocketAddress(PORT), 0);
        server.createContext("/api", new ApiHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + PORT);
    }

    static class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "";
            switch (exchange.getRequestMethod()) {
                case "GET":
                    response = handleGet(exchange);
                    break;
                case "POST":
                    response = handlePost(exchange);
                    break;
                case "DELETE":
                    response = handleDelete(exchange);
                    break;
                default:
                    response = "Unsupported request method";
            }
            sendResponse(exchange, response);
        }

        private String handleGet(HttpExchange exchange) throws IOException {
            String key = getKeyFromQuery(exchange.getRequestURI().getQuery());
            return dataStore.getOrDefault(key, "Not Found");
        }

        private String handlePost(HttpExchange exchange) throws IOException {
            String keyValue = readRequestBody(exchange.getRequestBody());
            String[] parts = keyValue.split("=", 2);
            if (parts.length < 2) return "Invalid input";
            dataStore.put(parts[0], parts[1]);
            return "Stored";
        }

        private String handleDelete(HttpExchange exchange) throws IOException {
            String key = getKeyFromQuery(exchange.getRequestURI().getQuery());
            if (dataStore.remove(key) != null) {
                return "Deleted";
            } else {
                return "Not Found";
            }
        }

        private String getKeyFromQuery(String query) {
            if (query == null) return "";
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length > 1 && keyValue[0].equals("key")) {
                    return keyValue[1];
                }
            }
            return "";
        }

        private String readRequestBody(InputStream inputStream) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }

        private void sendResponse(HttpExchange exchange, String response) throws IOException {
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}