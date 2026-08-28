package com.example.goldshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.goldshop.model.Product;

/**
 * ریپازیتوری برای مدیریت داده‌های کالا.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {}