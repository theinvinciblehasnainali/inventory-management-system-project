package com.inventory.dbconnectionhelper;
public class TestDBConnection {
    public static void main(String[] args) {
        if (DBConnection.getConnection() != null) {
            System.out.println("Connected to the database successfully!");
        } else {
            System.out.println("Failed to connect.");
        }
    }
}
