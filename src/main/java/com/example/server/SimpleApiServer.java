package com.example.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@RestController
@RequestMapping("/api/messages")
public class SimpleApiServer {

    private List<String> messages = new ArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(SimpleApiServer.class, args);
    }

    @PostMapping
    public void createMessage(@RequestBody String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message body cannot be empty");
        }
        messages.add(message);
    }

    @GetMapping
    public List<String> getMessages() {
        return messages;
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessages() {
        messages.clear();
    }
}