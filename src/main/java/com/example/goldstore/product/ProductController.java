package com.example.goldstore.product;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST endpoints for managing products in memory.
 */
@RestController
public class ProductController {

    private final Map<Long, Product> products = new ConcurrentHashMap<>();

    /**
     * Adds a product to the in-memory store.
     *
     * @param product product received from the request body
     * @return the stored product with a 201 Created status
     */
    @PostMapping("/products")
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        products.put(product.getId(), product);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * Returns all products currently held in memory.
     *
     * @return a snapshot of the stored products
     */
    @GetMapping("/products")
    public Collection<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }
}
