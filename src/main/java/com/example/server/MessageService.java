package com.example.server;

import java.util.concurrent.CopyOnWriteArrayList;

public class MessageService {
    private final CopyOnWriteArrayList<String> messages;

    public MessageService() {
        this.messages = new CopyOnWriteArrayList<>();
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public String getAllMessages() {
        return String.join("\n", messages);
    }
}