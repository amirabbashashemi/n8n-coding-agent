package com.example.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class MessageService {
    private final Map<String, String> messages = new ConcurrentHashMap<>();

    public void addMessage(String id, String message) {
        messages.put(id, message);
    }

    public String getMessage(String id) {
        return messages.get(id);
    }

    public void clearMessages() {
        messages.clear();
    }
}