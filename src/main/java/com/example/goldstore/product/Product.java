package com.example.goldstore.product;

import java.math.BigDecimal;

public class Product {
    private Long id;
    private String name;
    private BigDecimal weight;
    private BigDecimal price;

    public Product() {
    }

    public Product(Long id, String name, BigDecimal weight, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.weight = weight;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
