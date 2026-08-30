package com.goldshop.controller;

import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    // In-memory storage for products
    private final Map<Long, Product> productMap = new ConcurrentHashMap<>();
    private long productIdCounter = 1;

    // POST endpoint for adding a product
    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        product.setId(productIdCounter++);
        productMap.put(product.getId(), product);
        return product;
    }

    // GET endpoint for retrieving all products
    @GetMapping
    public List<Product> getAllProducts() {
        return new ArrayList<>(productMap.values());
    }
}