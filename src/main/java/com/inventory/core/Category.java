package com.inventory.core;

public class Category {
    private int id;
    private String name;

    public Category() {}

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Setters (optional, if you want mutability)
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    // ToString for debugging
    @Override
    public String toString() {
        return "\nID: " + id + ",\nName: " + name + '\n';
    }
}
