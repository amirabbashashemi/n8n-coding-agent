package com.example.server;

import com.example.server.http.HealthHandler;
import com.example.server.http.JsonSupport;
import com.example.server.http.MessageHandler;
import com.example.server.repository.InMemoryMessageRepository;
import com.example.server.repository.MessageRepository;
import com.example.server.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application entry point and composition root for the HTTP message service.
 */
public final class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final int PORT = 9000;

    private Main() {
        // Utility class; do not instantiate.
    }

    /**
     * Creates the application graph, starts the HTTP server, and installs its
     * graceful shutdown hook.
     *
     * @param args command-line arguments (currently unused)
     */
    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        JsonSupport jsonSupport = new JsonSupport(objectMapper);

        MessageRepository repository = new InMemoryMessageRepository();
        MessageService messageService = new MessageService(repository);
        MessageHandler messageHandler = new MessageHandler(messageService, jsonSupport);
        HealthHandler healthHandler = new HealthHandler(jsonSupport);

        final HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Unable to bind HTTP server to port " + PORT, exception);
            System.exit(1);
            return;
        }

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        server.createContext("/api/messages", messageHandler);
        server.createContext("/api/health", healthHandler);
        server.setExecutor(executor);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down HTTP server");
            server.stop(0);
            executor.shutdown();
        }, "message-server-shutdown"));

        try {
            server.start();
            LOGGER.info("HTTP server started on port " + PORT);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Unable to start HTTP server", exception);
            server.stop(0);
            executor.shutdown();
            System.exit(1);
        }
    }
}
