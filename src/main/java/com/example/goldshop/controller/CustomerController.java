package com.example.goldshop.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        // Logic to retrieve all customers
        return new ResponseEntity<>(/* list of customers */, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        // Logic to retrieve a customer by ID
        return new ResponseEntity<>(/* customer */, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        // Logic to create a new customer
        return new ResponseEntity<>(/* created customer */, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        // Logic to update a customer
        return new ResponseEntity<>(/* updated customer */, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        // Logic to delete a customer
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}