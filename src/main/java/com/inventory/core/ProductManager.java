package com.inventory.core;

import com.inventory.db.Database;
import com.inventory.exceptions.ProductNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductManager {

    // 1. Add a product
    public boolean addProduct(String name, int categoryId, int quantity, double price) {
        if (!new CategoryManager().categoryExists(categoryId)) {
            System.out.println("Error: Category ID " + categoryId + " does not exist.");
            return false;
        }

        String query = "INSERT INTO products (name, category_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setInt(2, categoryId);
            stmt.setInt(3, quantity);
            stmt.setDouble(4, price);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error adding product: " + e.getMessage());
            return false;
        }
    }


    // 2. List all products
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products ORDER BY id";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("category_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error listing products: " + e.getMessage());
        }

        return products;
    }

    // 3. Search products by name
    public List<Product> searchProducts(String keyword) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products WHERE name ILIKE ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("category_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error searching products: " + e.getMessage());
        }

        return products;
    }

    // 4. Update product


    public boolean partialUpdateProduct(int productId, String newName, Integer newCategoryId, Integer newQuantity, Double newPrice) {
        StringBuilder query = new StringBuilder("UPDATE products SET ");
        List<Object> parameters = new ArrayList<>();

        if (newName != null && !newName.isEmpty()) {
            query.append("name = ?, ");
            parameters.add(newName);
        }
        if (newCategoryId != null) {
            query.append("category_id = ?, ");
            parameters.add(newCategoryId);
        }
        if (newQuantity != null) {
            query.append("quantity = ?, ");
            parameters.add(newQuantity);
        }
        if (newPrice != null) {
            query.append("price = ?, ");
            parameters.add(newPrice);
        }

        // No fields to update
        if (parameters.isEmpty()) {
            return false;
        }

        // Remove last comma and space
        query.setLength(query.length() - 2);
        query.append(" WHERE id = ?");
        parameters.add(productId);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    public boolean partialUpdateProduct(String productName, String newName, Integer newCategoryId, Integer newQuantity, Double newPrice) {
        StringBuilder query = new StringBuilder("UPDATE products SET ");
        List<Object> parameters = new ArrayList<>();

        if (newName != null && !newName.isEmpty()) {
            query.append("name = ?, ");
            parameters.add(newName);
        }
        if (newCategoryId != null) {
            query.append("category_id = ?, ");
            parameters.add(newCategoryId);
        }
        if (newQuantity != null) {
            query.append("quantity = ?, ");
            parameters.add(newQuantity);
        }
        if (newPrice != null) {
            query.append("price = ?, ");
            parameters.add(newPrice);
        }

        // No fields to update
        if (parameters.isEmpty()) {
            return false;
        }

        // Remove last comma and space
        query.setLength(query.length() - 2);
        query.append(" WHERE name = ?");
        parameters.add(productName);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }





    // 5. Delete product
    public boolean deleteProductById(int id) {
        String query = "DELETE FROM products WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting product: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteProductByName(String name){
        String query = "DELETE FROM products WHERE name ILIKE ?";
        try(Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setString(1, name);
            return stmt.executeUpdate() > 0;
        }
        catch(SQLException e){
            System.out.println("Error deleting product: " + e.getMessage());
            return false;
        }
    }

    public Product getProductById(int id) throws ProductNotFoundException {
        String query = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("category_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error fetching product by ID: " + e.getMessage());
        }

        throw new ProductNotFoundException("Product not found with ID: " + id);
    }

}
