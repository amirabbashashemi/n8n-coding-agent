package com.example.server;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** HTTP endpoints for reading and publishing messages. */
@RestController
@RequestMapping("/api/messages")
public class MessageHandler {

    private final MessageService messageService;

    public MessageHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ResponseEntity<List<Message>> getMessages(
            @RequestParam(value = "userId", required = false) String userId) {
        return ResponseEntity.ok(messageService.getMessages());
    }

    @PostMapping
    public ResponseEntity<Message> createMessage(@RequestBody CreateMessageRequest request) {
        Message message = messageService.createMessage(
                request.sender(), request.recipient(), request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }
}
