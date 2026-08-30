package com.example.goldshop.controller;

import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private Map<Integer, Product> productMap = new HashMap<>();
    private int productIdCounter = 1;

    @PostMapping("/add")
    public Product addProduct(@RequestBody Product product) {
        product.setId(productIdCounter++);
        productMap.put(product.getId(), product);
        return product;
    }

    @GetMapping("/search")
    public List<Product> searchProducts() {
        return new ArrayList<>(productMap.values());
    }
}

class Product {
    private int id;
    private String name;
    private double price;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}