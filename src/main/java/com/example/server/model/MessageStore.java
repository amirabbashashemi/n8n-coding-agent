package com.example.server.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessageStore {
    private List<String> messages;

    public MessageStore() {
        messages = new CopyOnWriteArrayList<>();
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public List<String> getMessages() {
        return messages;
    }
}