package com.example.goldshop.product.service;

import org.springframework.stereotype.Service;
import com.example.goldshop.product.model.Product;
import com.example.goldshop.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    // Other service methods related to Product operations...
}