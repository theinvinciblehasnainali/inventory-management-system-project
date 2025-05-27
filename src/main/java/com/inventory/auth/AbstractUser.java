package com.inventory.auth;

public abstract class AbstractUser implements Authenticatable {
    private String username;
    private String hashedPassword;

    public AbstractUser(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean authenticate(String inputPassword) {
        String hashedInput = PasswordUtil.hashPassword(inputPassword);
        return this.hashedPassword.equals(hashedInput);
    }

    public abstract String getRole();
}
