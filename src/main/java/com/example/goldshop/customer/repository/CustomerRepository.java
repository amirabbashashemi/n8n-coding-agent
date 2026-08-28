package com.example.goldshop.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.goldshop.customer.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // می‌توانید متدهای مخصوص به عملیات کاربر را اینجا اضافه کنید
}