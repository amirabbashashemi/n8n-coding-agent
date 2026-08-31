package com.example.server;

import java.util.concurrent.CopyOnWriteArrayList;

public class MessageStore {
    private final CopyOnWriteArrayList<String> messages;

    public MessageStore() {
        this.messages = new CopyOnWriteArrayList<>();
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public String getMessages() {
        return String.join("\n", messages);
    }
}