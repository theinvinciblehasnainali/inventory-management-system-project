package com.inventory.core;

import com.inventory.db.Database;
import com.inventory.exceptions.CategoryNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryManager {

    // 1. Add Category
    public boolean addCategory(String name) {
        String query = "INSERT INTO categories (name) VALUES (?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error adding category: " + e.getMessage());
            return false;
        }
    }

    // 2. Get All Categories as List
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT id, name FROM categories ORDER BY id";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("name")));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving categories: " + e.getMessage());
        }

        return categories;
    }


    // 3. Delete Category by ID
    public boolean deleteCategoryByID(int categoryId) {
        String query = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, categoryId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting category: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteCategoryByName(String name) {
        String query = "DELETE FROM categories WHERE name ILIKE ?";
        try(Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setString(1, name);
            return stmt.executeUpdate() > 0;
        }
        catch (SQLException e) {
            System.out.println("Error deleting category: " + e.getMessage());
            return false;
        }
    }

    // 4. Get Category ID by Name
    public Integer getCategoryIdByName(String name) {
        String query = "SELECT id FROM categories WHERE name = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            System.out.println("Error getting category ID: " + e.getMessage());
        }
        return null;
    }

    // 5. Update Category Name
    public boolean updateCategoryName(int categoryId, String newName) {
        String query = "UPDATE categories SET name = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newName);
            stmt.setInt(2, categoryId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating category: " + e.getMessage());
            return false;
        }
    }
    public boolean updateCategoryName(String categoryName, String newName) {
        String query = "UPDATE categories SET name = ? WHERE name = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newName);
            stmt.setString(2, categoryName);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating category: " + e.getMessage());
            return false;
        }
    }

    // 6. Check if Category Exists by Name
    public boolean categoryExists(String name) {
        return getCategoryIdByName(name) != null;
    }

    public boolean categoryExists(int categoryId) {
        try{
            return getCategoryNameById(categoryId) != null;}
        catch (CategoryNotFoundException e) {
            return false;
        }
    }

    // 7. Search Categories by Keyword
    public List<Category> searchCategories(String keyword) {
        List<Category> results = new ArrayList<>();
        String query = "SELECT id, name FROM categories WHERE name ILIKE ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(new Category(rs.getInt("id"), rs.getString("name")));
            }

        } catch (SQLException e) {
            System.out.println("Error searching categories: " + e.getMessage());
        }

        return results;
    }

    // 8. Get Category Name by ID
    public String getCategoryNameById(int categoryId) throws CategoryNotFoundException {
        String query = "SELECT name FROM categories WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching category name: " + e.getMessage());
        }
        throw new CategoryNotFoundException("Category not found for id: " + categoryId);
    }

    public Category getCategoryById(int categoryId) {
        String query = "SELECT id, name FROM categories WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Category(rs.getInt("id"), rs.getString("name"));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching category: " + e.getMessage());
        }
        return null;
    }


}
