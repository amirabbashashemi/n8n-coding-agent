package com.example.goldshop.repository;

import com.example.goldshop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // اینجا می‌توانید متدهای سفارشی را اضافه کنید
}