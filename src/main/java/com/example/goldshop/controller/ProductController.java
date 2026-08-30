package com.example.goldshop.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for managing gold products in memory.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ConcurrentMap<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    /**
     * Adds a product and assigns an identifier when the request does not provide one.
     */
    @PostMapping("/api/products")
    public ResponseEntity<?> addProduct(@RequestBody Product product) {
        if (product == null || product.name() == null || product.name().isBlank()
                || product.price() == null || product.price().signum() < 0
                || product.goldWeight() == null || product.goldWeight().signum() <= 0) {
            return ResponseEntity.badRequest().body("name, price, and a positive goldWeight are required");
        }

        long id = product.id() == null ? nextId.getAndIncrement() : product.id();
        if (id <= 0) {
            return ResponseEntity.badRequest().body("id must be positive when provided");
        }

        Product storedProduct = new Product(id, product.name().trim(), product.price(), product.goldWeight());
        products.put(id, storedProduct);
        return ResponseEntity.created(URI.create("/api/products/" + id)).body(storedProduct);
    }

    /**
     * Returns all products currently held in memory.
     */
    @GetMapping("/api/products")
    public List<Product> getAllProducts() {
        return List.copyOf(products.values());
    }

    /**
     * Product data exchanged by the API. Prices and weights use BigDecimal for accuracy.
     */
    public record Product(Long id, String name, BigDecimal price, BigDecimal goldWeight) {
    }
}
