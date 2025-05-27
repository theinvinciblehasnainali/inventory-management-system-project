package com.inventory.auth;

public interface Authenticatable {
    boolean authenticate(String password);
}
