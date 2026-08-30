package com.example.goldstore.product;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final Map<Long, Product> products = new ConcurrentHashMap<>();

    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        if (product == null || product.getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        products.put(product.getId(), product);
        return ResponseEntity.created(URI.create("/api/products/" + product.getId()))
                .body(product);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(new ArrayList<>(products.values()));
    }
}
