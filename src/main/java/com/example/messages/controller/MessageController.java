package com.example.messages.controller;

import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final ConcurrentMap<Long, String> messages;
    private final AtomicLong messageIdGenerator;

    public MessageController() {
        this.messages = new ConcurrentHashMap<>();
        this.messageIdGenerator = new AtomicLong();
    }

    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Void> addMessage(@RequestBody String message) {
        long messageId = messageIdGenerator.incrementAndGet();
        messages.put(messageId, message);
        return ResponseEntity.status(201).build();
    }

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getMessages() {
        String messageContent = messages.entrySet().stream()
                .sorted(Comparator.comparingLong(entry -> entry.getKey()))
                .map(entry -> entry.getValue())
                .collect(Collectors.joining(System.lineSeparator()));

        return ResponseEntity.ok(messageContent);
    }
}
