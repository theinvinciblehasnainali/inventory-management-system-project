package com.inventory.core;

import com.inventory.core.StockMovement.ChangeType;
import com.inventory.db.Database;
import com.inventory.exceptions.StockMovementNotFoundException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class StockMovementManager {

    private static final Logger logger = Logger.getLogger(StockMovementManager.class.getName());

    public boolean addStockMovement(int productId, ChangeType changeType, int quantity) {
        if (quantity <= 0) {
            logger.warning("Quantity must be greater than 0.");
            return false;
        }

        String query = "INSERT INTO stock_movements (product_id, change_type, quantity, movement_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, productId);
            stmt.setString(2, changeType.name());
            stmt.setInt(3, quantity);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            logger.severe("Error adding stock movement: " + e.getMessage());
            return false;
        }
    }

    public List<StockMovement> getAllStockMovements() {
        List<StockMovement> movements = new ArrayList<>();
        String query = "SELECT * FROM stock_movements ORDER BY movement_time DESC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                movements.add(new StockMovement(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        ChangeType.valueOf(rs.getString("change_type")),
                        rs.getInt("quantity"),
                        rs.getTimestamp("movement_time").toLocalDateTime()
                ));
            }

        } catch (SQLException e) {
            logger.severe("Error retrieving stock movements: " + e.getMessage());
        }

        return movements;
    }

    public List<StockMovement> getMovementsByProductId(int productId) {
        List<StockMovement> results = new ArrayList<>();
        String query = "SELECT * FROM stock_movements WHERE product_id = ? ORDER BY movement_time DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(new StockMovement(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        ChangeType.valueOf(rs.getString("change_type")),
                        rs.getInt("quantity"),
                        rs.getTimestamp("movement_time").toLocalDateTime()
                ));
            }

        } catch (SQLException e) {
            logger.severe("Error retrieving stock movements: " + e.getMessage());
        }

        return results;
    }

    // Update product quantity and log movement
    public boolean updateProductStockAndLogMovement(int productId, ChangeType changeType, int quantity) {
        String updateQuery = (changeType == ChangeType.IN)
                ? "UPDATE products SET quantity = quantity + ? WHERE id = ?"
                : "UPDATE products SET quantity = quantity - ? WHERE id = ?";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false); // Transaction block

            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                 PreparedStatement logStmt = conn.prepareStatement(
                         "INSERT INTO stock_movements (product_id, change_type, quantity, movement_time) VALUES (?, ?, ?, ?)")) {

                // Update quantity
                updateStmt.setInt(1, quantity);
                updateStmt.setInt(2, productId);
                int affected = updateStmt.executeUpdate();

                if (affected == 0) {
                    conn.rollback();
                    logger.warning("No product updated. ID may be invalid.");
                    return false;
                }

                // Log movement
                logStmt.setInt(1, productId);
                logStmt.setString(2, changeType.name());
                logStmt.setInt(3, quantity);
                logStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                logStmt.executeUpdate();

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                logger.severe("Transaction failed: " + e.getMessage());
                return false;
            }

        } catch (SQLException e) {
            logger.severe("Connection error: " + e.getMessage());
            return false;
        }
    }
    public List<StockMovement> getMovementsBetweenDates(LocalDateTime start, LocalDateTime end){
        List<StockMovement> results = new ArrayList<>();
        String query = "SELECT * FROM stock_movements WHERE movement_time BETWEEN ? AND ? ORDER BY movement_time DESC";
        try(Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(start));
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(new StockMovement(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        ChangeType.valueOf(rs.getString("change_type").toUpperCase()),
                        rs.getInt("quantity"),
                        rs.getTimestamp("movement_time").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error filtering movements by date: " + e.getMessage());
        }
        return results;
    }

    public List<StockMovement> getMovementsByType(String changeType) {
        List<StockMovement> results = new ArrayList<>();
        String query = "SELECT * FROM stock_movements WHERE change_type = ? ORDER BY movement_time DESC";
        try(Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setString(1, changeType.toUpperCase());
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                results.add(new StockMovement(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        ChangeType.valueOf(rs.getString("change_type").toUpperCase()),
                        rs.getInt("quantity"),
                        rs.getTimestamp("movement_time").toLocalDateTime()
                ));
            }
        }
        catch(SQLException e){
            System.out.println("Error filtering movements by type: " + e.getMessage());
        }
        return results;
    }

    public StockMovement getLatestMovementForProduct(int productId) throws StockMovementNotFoundException {
        String query = "SELECT * FROM stock_movements WHERE product_id = ? ORDER BY movement_time DESC";
        try(Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                return new StockMovement(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        ChangeType.valueOf(rs.getString("change_type").toUpperCase()),
                        rs.getInt("quantity"),
                        rs.getTimestamp("movement_time").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            System.out.println("Error filtering movements by product: " + e.getMessage());
        }
        throw new StockMovementNotFoundException("No stock movements found for Product ID: " + productId);
    }
}
