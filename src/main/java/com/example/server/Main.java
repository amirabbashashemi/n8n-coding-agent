package com.example.server;

import com.example.server.http.HealthHandler;
import com.example.server.http.JsonSupport;
import com.example.server.http.MessageHandler;
import com.example.server.repository.InMemoryMessageRepository;
import com.example.server.service.MessageService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        var repository = new InMemoryMessageRepository();
        var messageService = new MessageService(repository);
        var jsonSupport = new JsonSupport();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", new HealthHandler());
        server.createContext("/messages", new MessageHandler(messageService, jsonSupport));
        server.setExecutor(null);
        server.start();
        System.out.println("Message server listening on port " + port);
    }
}