package com.example.messages.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final Map<Long, String> messages = new ConcurrentSkipListMap<>();
    private final AtomicLong nextMessageId = new AtomicLong();

    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public synchronized ResponseEntity<String> addMessage(@RequestBody(required = false) String message) {
        if (message == null || message.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        messages.put(nextMessageId.getAndIncrement(), message);
        return ResponseEntity.status(201).build();
    }

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public synchronized ResponseEntity<String> getMessages() {
        String body = messages.values().stream().collect(Collectors.joining("\n"));
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(body);
    }

    @DeleteMapping
    public synchronized ResponseEntity<Void> deleteMessages() {
        messages.clear();
        nextMessageId.set(0);
        return ResponseEntity.noContent().build();
    }
}
