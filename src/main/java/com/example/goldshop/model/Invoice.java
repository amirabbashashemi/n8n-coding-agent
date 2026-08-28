package com.example.goldshop.model;

public class Invoice {
    private Long id;
    private Long customerId;
    private Long productId;
    private Double amount;

    // Constructor
    public Invoice(Long id, Long customerId, Long productId, Double amount) {
        this.id = id;
        this.customerId = customerId;
        this.productId = productId;
        this.amount = amount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}