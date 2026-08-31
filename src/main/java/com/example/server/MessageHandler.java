package com.example.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** HTTP adapter for the message service using the JDK embedded HTTP server. */
public final class MessageHandler implements HttpHandler {
    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    public MessageHandler(ObjectMapper objectMapper, MessageService messageService) {
        this.objectMapper = objectMapper;
        this.messageService = messageService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            if ("GET".equalsIgnoreCase(method)) {
                handleGet(exchange);
            } else if ("POST".equalsIgnoreCase(method)) {
                handlePost(exchange);
            } else {
                send(exchange, 405, Map.of("error", "method not allowed"));
            }
        } catch (IllegalArgumentException e) {
            send(exchange, 400, Map.of("error", e.getMessage() == null ? "invalid request" : e.getMessage()));
        } catch (ReflectiveOperationException e) {
            send(exchange, 500, Map.of("error", "message service failure"));
        } finally {
            exchange.close();
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException, ReflectiveOperationException {
        String recipient = queryParameter(exchange.getRequestURI().getRawQuery(), "recipient");
        if (recipient == null || recipient.isBlank()) {
            send(exchange, 400, Map.of("error", "recipient is required"));
            return;
        }
        Object result = invokeService("findByRecipient", recipient, "getMessages", recipient, "messagesFor", recipient);
        send(exchange, 200, result);
    }

    private void handlePost(HttpExchange exchange) throws IOException, ReflectiveOperationException {
        try (InputStream body = exchange.getRequestBody()) {
            CreateMessageRequest request = objectMapper.readValue(body, CreateMessageRequest.class);
            String sender = stringProperty(request, "getSender", "sender");
            String recipient = stringProperty(request, "getRecipient", "recipient");
            if (sender == null || sender.isBlank() || recipient == null || recipient.isBlank()) {
                send(exchange, 400, Map.of("error", "sender and recipient are required"));
                return;
            }
            Object result = invokeService("createMessage", sender, recipient, "create", sender, recipient, "send", sender, recipient);
            send(exchange, 201, result);
        }
    }

    private Object invokeService(String firstName, Object firstArg, String secondName, Object secondArg,
                                 String thirdName, Object thirdArg, String fourthName, Object fourthArg) throws ReflectiveOperationException {
        for (String name : new String[]{firstName, secondName, thirdName, fourthName}) {
            for (Method method : messageService.getClass().getMethods()) {
                if (method.getName().equals(name) && method.getParameterCount() == 1) {
                    return method.invoke(messageService, firstArg);
                }
                if (method.getName().equals(name) && method.getParameterCount() == 2) {
                    return method.invoke(messageService, firstArg, secondArg);
                }
            }
        }
        throw new NoSuchMethodException("No compatible message service method");
    }

    private static String stringProperty(Object value, String getter, String field) {
        try {
            Method method = value.getClass().getMethod(getter);
            Object result = method.invoke(value);
            return result == null ? null : result.toString();
        } catch (ReflectiveOperationException ignored) {
            try {
                Object result = value.getClass().getField(field).get(value);
                return result == null ? null : result.toString();
            } catch (ReflectiveOperationException ignoredAgain) {
                return null;
            }
        }
    }

    private void send(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = objectMapper.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static String queryParameter(String query, String key) {
        if (query == null) return null;
        for (String part : query.split("&")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2 && pair[0].equals(key)) return pair[1];
        }
        return null;
    }
}
