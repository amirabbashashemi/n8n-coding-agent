package com.example.goldshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.goldshop.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // افزودن روش‌های اختصاصی در صورت نیاز در اینجا
}