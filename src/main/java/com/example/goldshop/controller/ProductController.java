package com.example.goldshop.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final Map<Long, Product> productMap = new HashMap<>();
    private long currentId = 1;

    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        product.setId(currentId++);
        productMap.put(product.getId(), product);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return new ResponseEntity<>(new ArrayList<>(productMap.values()), HttpStatus.OK);
    }
}

class Product {
    private Long id;
    private String name;
    private double price;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}