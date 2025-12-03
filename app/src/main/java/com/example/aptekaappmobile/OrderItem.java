package com.example.aptekaappmobile;

import java.util.Date;

public class OrderItem {
    private int id;
    private int orderId;
    private int productId;
    private int quantity;
    private double price;
    private Date createdAt;

    public OrderItem(int id, int orderId, int productId, int quantity, double price, Date createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getOrderId() { return orderId; }
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public Date getCreatedAt() { return createdAt; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}