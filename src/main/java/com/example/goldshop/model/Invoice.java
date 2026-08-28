package com.example.goldshop.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    private Long id;
    private LocalDate date;
    private String customer;

    // Constructors
    public Invoice() {}

    public Invoice(Long id, LocalDate date, String customer) {
        this.id = id;
        this.date = date;
        this.customer = customer;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }
}