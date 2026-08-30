package com.goldshop.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final Map<Long, Product> productMap = new HashMap<>();
    private long currentId = 1;

    @PostMapping
    public void addProduct(@RequestBody Product product) {
        product.setId(currentId++);
        productMap.put(product.getId(), product);
    }

    @GetMapping
    public Collection<Product> getAllProducts() {
        return productMap.values();
    }
}