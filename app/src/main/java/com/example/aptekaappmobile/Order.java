package com.example.aptekaappmobile;

import java.util.Date;

public class Order {
    private int id;
    private int userId;
    private String status;
    private Date createdAt;
    private String contactPhone;

    public Order(int id, int userId, String status, Date createdAt, String contactPhone) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.createdAt = createdAt;
        this.contactPhone = contactPhone;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getStatus() { return status; }
    public Date getCreatedAt() { return createdAt; }
    public String getContactPhone() { return contactPhone; }
}
