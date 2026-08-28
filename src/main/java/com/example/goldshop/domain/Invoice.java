package com.example.goldshop.domain;

import java.util.Date;
import java.util.List;

public class Invoice {
    private Date date;
    private String customer;
    private List<String> productList;

    public Invoice(Date date, String customer, List<String> productList) {
        this.date = date;
        this.customer = customer;
        this.productList = productList;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public List<String> getProductList() {
        return productList;
    }

    public void setProductList(List<String> productList) {
        this.productList = productList;
    }
}