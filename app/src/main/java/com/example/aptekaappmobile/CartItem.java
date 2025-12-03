package com.example.aptekaappmobile;

// CartItem.java
public class CartItem {
    public int productId;
    public String name;
    public double price;
    public int quantity = 1;

    public CartItem(int productId, String name, double price) {
        this.productId = productId;
        this.name = name;
        this.price = price;
    }

    public double getTotal() {
        return price * quantity;
    }
}