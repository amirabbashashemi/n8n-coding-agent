package com.example.goldstore.product;

import java.math.BigDecimal;

/**
 * Represents a product available in the gold store.
 *
 * <p>The no-argument constructor and JavaBean accessors allow Spring/Jackson
 * to bind request JSON and serialize response JSON.</p>
 */
public class Product {

    private Long id;
    private String name;
    private BigDecimal price;

    public Product() {
        // Required for JSON deserialization.
    }

    public Product(Long id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
