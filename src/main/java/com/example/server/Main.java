package com.example.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Application entry point for the HTTP message service.
 */
public final class Main {
    private static final int PORT = 9000;

    private Main() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        MessageRepository repository = new InMemoryMessageRepository();
        MessageService service = new MessageService(repository);
        MessageHandler handler = new MessageHandler(objectMapper, service);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        ExecutorService executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor();
        server.setExecutor(executor);
        server.createContext("/api", handler);

        CountDownLatch shutdownSignal = new CountDownLatch(1);
        Thread shutdownHook = new Thread(() -> {
            server.stop(0);
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException interruptedException) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            } finally {
                shutdownSignal.countDown();
            }
        }, "message-server-shutdown");

        Runtime.getRuntime().addShutdownHook(shutdownHook);
        server.start();
        shutdownSignal.await();
    }
}
