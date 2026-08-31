package com.example.server;

import com.example.server.http.HealthHandler;
import com.example.server.http.JsonHttpResponder;
import com.example.server.http.MessageHandler;
import com.example.server.repository.InMemoryMessageRepository;
import com.example.server.repository.MessageRepository;
import com.example.server.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Application entry point and HTTP server bootstrap.
 */
public final class Main {

    private static final String BIND_ADDRESS = "0.0.0.0";
    private static final int PORT = 9000;

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        MessageRepository messageRepository = new InMemoryMessageRepository();
        MessageService messageService = new MessageService(messageRepository);
        JsonHttpResponder responder = new JsonHttpResponder(objectMapper);
        MessageHandler messageHandler = new MessageHandler(messageService, objectMapper, responder);
        HealthHandler healthHandler = new HealthHandler(responder);

        HttpServer server = HttpServer.create(
                new InetSocketAddress(BIND_ADDRESS, PORT),
                0
        );
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        AtomicBoolean stopped = new AtomicBoolean(false);

        server.createContext("/api/messages", messageHandler);
        server.createContext("/api/health", healthHandler);
        server.setExecutor(executor);

        Thread shutdownHook = new Thread(() -> stop(server, executor, stopped), "http-server-shutdown");
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        try {
            server.start();
        } catch (RuntimeException | Error startupFailure) {
            stop(server, executor, stopped);
            throw startupFailure;
        }
    }

    private static void stop(HttpServer server, ExecutorService executor, AtomicBoolean stopped) {
        if (stopped.compareAndSet(false, true)) {
            server.stop(0);
            executor.close();
        }
    }
}
