package com.inventory.auth;

public class Admin extends AbstractUser {
    public Admin(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}
