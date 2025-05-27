package com.inventory.exceptions;

public class StockMovementNotFoundException extends Exception {
    public StockMovementNotFoundException(String message) {
        super(message);
    }
}
