package com.example.goldshop.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.goldshop.product.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // متدهای اضافی برای عملیات خاص روی کالا می‌توانند در اینجا تعریف شوند
}