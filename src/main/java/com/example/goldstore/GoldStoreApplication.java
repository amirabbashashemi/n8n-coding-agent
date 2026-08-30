package com.example.goldstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Gold Store Spring Boot application.
 */
@SpringBootApplication
public class GoldStoreApplication {

    private GoldStoreApplication() {
        // Utility-style bootstrap class; do not instantiate.
    }

    public static void main(String[] args) {
        SpringApplication.run(GoldStoreApplication.class, args);
    }
}
