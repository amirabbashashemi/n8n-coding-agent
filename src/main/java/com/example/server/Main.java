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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Application entry point for the HTTP message service. */
public final class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final int PORT = 9000;
    private static final int BACKLOG = 100;

    private Main() {
    }

    public static void main(String[] args) {
        MessageRepository repository = new InMemoryMessageRepository();
        MessageService messageService = new MessageService(repository);
        ObjectMapper objectMapper = new ObjectMapper();
        MessageHandler messageHandler = new MessageHandler(messageService, objectMapper);
        HealthHandler healthHandler = new HealthHandler(objectMapper);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        final HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), BACKLOG);
        } catch (IOException exception) {
            executor.close();
            LOGGER.log(Level.SEVERE, "Unable to bind HTTP server to port " + PORT, exception);
            return;
        }

        server.createContext("/api/messages", messageHandler);
        server.createContext("/api/health", healthHandler);
        server.setExecutor(executor);

        CountDownLatch shutdown = new CountDownLatch(1);
        Thread shutdownHook = new Thread(() -> {
            LOGGER.info("Stopping HTTP server");
            server.stop(0);
            executor.close();
            shutdown.countDown();
        }, "http-server-shutdown");
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        server.start();
        LOGGER.info(() -> "HTTP server started on port " + PORT);

        try {
            shutdown.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            server.stop(0);
            executor.close();
        }
    }
}
