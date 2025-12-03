package com.example.aptekaappmobile;

public class User {
    private int id;
    private String login;
    private String password;
    private String phone;
    private boolean isBlocked;

    public User(int id, String login, String password, String phone, boolean isBlocked) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.phone = phone;
        this.isBlocked = isBlocked;
    }

    public int getId() { return id; }
    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public String getPhone() { return phone; }
    public boolean isBlocked() { return isBlocked; }
}
