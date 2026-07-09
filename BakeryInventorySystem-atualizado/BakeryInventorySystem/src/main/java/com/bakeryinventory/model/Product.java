package com.bakeryinventory.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Product {
    private int id;
    private String name;
    private String category;
    private int quantity;
    private int idealQuantity;
    private BigDecimal unitPrice;
    private LocalDate expirationDate;
    private LocalDate registrationDate;

    public Product() {
    }

    public Product(int id, String name, String category, int quantity, int idealQuantity,
                   BigDecimal unitPrice, LocalDate expirationDate, LocalDate registrationDate) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.idealQuantity = idealQuantity;
        this.unitPrice = unitPrice;
        this.expirationDate = expirationDate;
        this.registrationDate = registrationDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getIdealQuantity() {
        return idealQuantity;
    }

    public void setIdealQuantity(int idealQuantity) {
        this.idealQuantity = idealQuantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isBelowIdealStock() {
        return quantity < idealQuantity;
    }

    public boolean isExpired() {
        return expirationDate.isBefore(LocalDate.now());
    }

    public boolean isNearExpiration(int days) {
        LocalDate today = LocalDate.now();
        return !expirationDate.isBefore(today) && !expirationDate.isAfter(today.plusDays(days));
    }
}
