package com.inventory.core;

import java.time.LocalDateTime;

public class StockMovement {
    public enum ChangeType {
        IN, OUT
    }

    private int id;
    private int productId;
    private ChangeType changeType;
    private int quantity;
    private LocalDateTime movementTime;

    public StockMovement(int id, int productId, ChangeType changeType, int quantity, LocalDateTime movementTime) {
        this.id = id;
        this.productId = productId;
        this.changeType = changeType;
        this.quantity = quantity;
        this.movementTime = movementTime;
    }

    public int getId() { return id; }
    public int getProductId() { return productId; }
    public ChangeType getChangeType() { return changeType; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getMovementTime() { return movementTime; }

    public void setId(int id) { this.id = id; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setChangeType(ChangeType changeType) { this.changeType = changeType; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setMovementTime(LocalDateTime movementTime) { this.movementTime = movementTime; }

    @Override
    public String toString() {
        return "\nID: " + id +
                ",\nProduct ID: " + productId +
                ",\nChange Type: " + changeType +
                ",\nQuantity: " + quantity +
                ",\nMovement Time: " + movementTime + '\n';
    }

}
