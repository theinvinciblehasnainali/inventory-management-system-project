package com.inventory.core;

public class Product {
    private int id;
    private String name;
    private int categoryId;
    private int quantity;
    private double price;

    // No-arg constructor (optional, useful for frameworks or dynamic population)
    public Product() {}

    // All-args constructor
    public Product(int id, String name, int categoryId, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getCategoryId() { return categoryId; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() {
        return "\nID: " + id +
                ",\nName: " + name +
                ",\nCategory ID: " + categoryId +
                ",\n Quantity: " + quantity +
                ",\nPrice: $" + price +
                '\n';
    }
}
