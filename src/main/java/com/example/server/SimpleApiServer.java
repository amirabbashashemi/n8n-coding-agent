package com.example.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import spark.Spark;

public class SimpleApiServer {
    private static final List<String> messages = new CopyOnWriteArrayList<>();
    
    public static void main(String[] args) {
        Spark.port(4567);
        Spark.post("/api/messages", (req, res) -> {
            String body = req.body();
            if (body == null || body.isEmpty()) {
                res.status(400);
                return "Bad Request";
            }
            messages.add(body);
            res.status(201);
            return "Message received";
        });
        
        Spark.get("/api/messages", (req, res) -> {
            return String.join("\n", messages);
        });
        
        Spark.delete("/api/messages", (req, res) -> {
            messages.clear();
            res.status(204);
            return "";
        });
    }
}