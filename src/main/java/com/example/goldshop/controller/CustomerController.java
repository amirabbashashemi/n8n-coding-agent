package com.example.goldshop.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    // این لیست به عنوان نمونه ای از داده ها مورد استفاده قرار می گیرد
    private List<String> customers = new ArrayList<>();

    @GetMapping
    public List<String> getAllCustomers() {
        return customers;
    }

    @PostMapping
    public void addCustomer(@RequestBody String customer) {
        customers.add(customer);
    }

    @DeleteMapping("/{id}")
    public void deleteCustomer(@PathVariable int id) {
        if (id >= 0 && id < customers.size()) {
            customers.remove(id);
        }
    }
}