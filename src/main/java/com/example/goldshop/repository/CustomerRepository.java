package com.example.goldshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.goldshop.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // اینجا می‌توانید متدهای دلخواه خود را برای دسترسی به داده‌های مشتریان اضافه کنید
}