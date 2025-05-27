package com.inventory.auth;

public class Employee extends AbstractUser {
    public Employee(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRole() {
        return "EMPLOYEE";
    }
}
