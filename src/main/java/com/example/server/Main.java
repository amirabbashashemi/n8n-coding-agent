package com.example.server;

import com.example.server.http.HealthHandler;
import com.example.server.http.JsonSupport;
import com.example.server.http.MessageHandler;
import com.example.server.repository.InMemoryMessageRepository;
import com.example.server.repository.MessageRepository;
import com.example.server.service.MessageService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Application entry point for the lightweight message server.
 */
public final class Main {

    private static final String HOST = "0.0.0.0";
    private static final int PORT = 9000;
    private static final int SHUTDOWN_GRACE_SECONDS = 5;

    private Main() {
        // Utility class; do not instantiate.
    }

    public static void main(String[] args) {
        final MessageRepository repository = new InMemoryMessageRepository();
        final MessageService messageService = new MessageService(repository);
        final JsonSupport jsonSupport = new JsonSupport();
        final MessageHandler messageHandler = new MessageHandler(messageService, jsonSupport);
        final HealthHandler healthHandler = new HealthHandler(jsonSupport);

        final HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(HOST, PORT), 0);
        } catch (BindException exception) {
            System.err.printf("Unable to bind HTTP server to %s:%d: %s%n", HOST, PORT,
                    exception.getMessage());
            System.exit(1);
            return;
        } catch (IOException exception) {
            System.err.printf("Unable to start HTTP server on %s:%d: %s%n", HOST, PORT,
                    exception.getMessage());
            System.exit(1);
            return;
        }

        server.createContext("/api/messages", messageHandler);
        server.createContext("/api/health", healthHandler);

        final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        server.setExecutor(executor);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop(SHUTDOWN_GRACE_SECONDS);
            executor.close();
        }, "message-server-shutdown"));

        server.start();
        System.out.printf("Message server started on http://%s:%d%n", HOST, PORT);
    }
}
