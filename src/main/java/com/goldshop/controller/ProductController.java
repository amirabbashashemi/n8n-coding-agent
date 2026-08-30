package com.goldshop.controller;

import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final Map<Long, Product> productMap = new ConcurrentHashMap<>();
    private long currentId = 1;

    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        product.setId(currentId++);
        productMap.put(product.getId(), product);
        return product;
    }

    @GetMapping
    public List<Product> getProducts() {
        return new ArrayList<>(productMap.values());
    }
}

class Product {
    private Long id;
    private String name;
    private double price;
    private String description;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}