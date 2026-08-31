package com.example.server;

import com.example.server.handler.HealthHandler;
import com.example.server.handler.MessageHandler;
import com.example.server.repository.InMemoryMessageRepository;
import com.example.server.repository.MessageRepository;
import com.example.server.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Application entry point for the HTTP message server.
 */
public final class Main {
    private static final int PORT = 9000;
    private static final int BACKLOG = 0;
    private static final int SHUTDOWN_DELAY_SECONDS = 1;

    private Main() {
        // Utility class; do not instantiate.
    }

    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        MessageRepository repository = new InMemoryMessageRepository();
        MessageService messageService = new MessageService(repository);
        MessageHandler messageHandler = new MessageHandler(messageService, objectMapper);
        HealthHandler healthHandler = new HealthHandler(objectMapper);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), BACKLOG);
            server.createContext("/api/messages", messageHandler);
            server.createContext("/api/health", healthHandler);
            server.setExecutor(executor);
            server.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                server.stop(SHUTDOWN_DELAY_SECONDS);
                executor.close();
            }, "message-server-shutdown"));
        } catch (IOException exception) {
            System.err.println("Failed to start HTTP server on port " + PORT + ": "
                    + exception.getMessage());
            executor.close();
            System.exit(1);
        }
    }
}
