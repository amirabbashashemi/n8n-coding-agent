package com.jewelrystore.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final Map<String, Product> productMap = new HashMap<>();

    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        productMap.put(product.getId(), product);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Map<String, Product>> getAllProducts() {
        return new ResponseEntity<>(productMap, HttpStatus.OK);
    }
}

class Product {
    private String id;
    private String name;
    private BigDecimal price;

    // Constructors, Getters, and Setters
    public Product(String id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}