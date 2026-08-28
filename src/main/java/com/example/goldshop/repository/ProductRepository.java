package com.example.goldshop.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.goldshop.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // اینجا می‌توانید متدهایی برای عملیات خاص محصول اضافه کنید
}