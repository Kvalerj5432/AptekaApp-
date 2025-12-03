package com.example.aptekaappmobile;

import java.util.Date;

public class Medicine {
    private int id;
    private String name;
    private double price;
    private String imageUrl;
    private Date createdAt;

    public Medicine(int id, String name, double price, String imageUrl, Date createdAt) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public Date getCreatedAt() { return createdAt; }
}

