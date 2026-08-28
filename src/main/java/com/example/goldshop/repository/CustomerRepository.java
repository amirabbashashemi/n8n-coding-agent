package com.example.goldshop.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.goldshop.customer.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Additional query methods can be defined here
}