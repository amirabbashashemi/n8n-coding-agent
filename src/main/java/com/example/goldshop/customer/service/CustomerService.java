package com.example.goldshop.customer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.goldshop.customer.model.Customer;
import com.example.goldshop.customer.repository.CustomerRepository;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    public void delete(Long id) {
        customerRepository.deleteById(id);
    }


    public Customer update(Long id, Customer customer) {
        Customer byId = customerRepository.findById(id).get();
        byId.setName(customer.getName());
        byId.setEmail(customer.getEmail());
        save(byId);
        return byId;
    }
}