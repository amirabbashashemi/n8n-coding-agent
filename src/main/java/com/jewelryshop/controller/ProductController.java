package com.jewelryshop.controller;

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
    
    private Map<String, Product> productStore = new HashMap<>();
    
    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        productStore.put(product.getId(), product);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return new ResponseEntity<>(new ArrayList<>(productStore.values()), HttpStatus.OK);
    }
}