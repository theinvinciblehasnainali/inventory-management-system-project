package com.inventory.auth;

import com.inventory.core.UserLog;
import com.inventory.db.Database;
import com.inventory.util.Logger;
import com.inventory.core.User; // NEW: Import the User DTO

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList; // NEW: Import ArrayList
import java.util.HashMap;
import java.util.List; // NEW: Import List

public class UserManager {
    private HashMap<String, AbstractUser> users = new HashMap<>();

    public UserManager() {
        // Predefined admin (This part is for initial setup, actual users come from DB)
        // Ensure this logic is handled carefully if you primarily rely on DB for users.
        String adminHashed = PasswordUtil.hashPassword("admin123");
        // This 'Admin' object might not be stored in the DB if not explicitly registered.
        // The GUI will read users from the DB.
        users.put("admin", new Admin("admin", adminHashed));
    }

    public boolean authenticate(String username, String password) {
        String query = "SELECT password FROM users WHERE username = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                return storedHash.equals(PasswordUtil.hashPassword(password));
            }

        } catch (SQLException e) {
            System.out.println("Error during authentication: " + e.getMessage());
        }

        return false;
    }


    public boolean registerUser(String username, String password, String role) {
        String hashedPassword = PasswordUtil.hashPassword(password);

        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role.toUpperCase());
            stmt.executeUpdate();
            Logger.log(username, "REGISTER");
            return true;

        } catch (SQLException e) {
            System.out.println("Error registering user: " + e.getMessage());
            return false;
        }
    }


    public String getUserRole(String username) {
        String query = "SELECT role FROM users WHERE username = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("role");
            }

        } catch (SQLException e) {
            System.out.println("Error fetching user role: " + e.getMessage());
        }

        return null;
    }

    private int countAdmins() {
        String query = "SELECT COUNT(*) AS admin_count FROM users WHERE role = 'ADMIN'";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("admin_count");
            }
        } catch (SQLException e) {
            System.out.println("Error counting admins: " + e.getMessage());
        }
        return 0;
    }

    public boolean deleteUser(String currentAdminUsername, String targetUsername) {
        if (currentAdminUsername.equals(targetUsername)) {
            System.out.println("You cannot delete your own admin account.");
            return false;
        }

        String targetRole = getUserRole(targetUsername);

        if (targetRole == null) {
            System.out.println("User does not exist.");
            return false;
        }

        if ("ADMIN".equalsIgnoreCase(targetRole)) {
            int adminCount = countAdmins();
            if (adminCount <= 1) {
                System.out.println("Cannot delete the last remaining admin account.");
                return false;
            }
        }

        // Proceed to delete user from database
        String query = "DELETE FROM users WHERE username = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, targetUsername);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("User deleted successfully.");
                Logger.log(targetUsername, "DELETE");
                return true;
            } else {
                System.out.println("User deletion failed.");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all users from the database for display in the GUI.
     * Returns a list of User DTOs (id, username, role).
     * NEW METHOD FOR GUI
     */
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String query = "SELECT id, username, role FROM users ORDER BY username ASC"; // Assuming 'id' column exists

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String role = rs.getString("role");
                userList.add(new User(id, username, role));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving user list for GUI: " + e.getMessage());
            // Consider logging this exception more robustly than just printing to console
        }
        return userList;
    }


    // This method is primarily for console output and can be removed if not needed elsewhere
    // as getAllUsers() will serve the GUI.
    public void listUsers() {
        String query = "SELECT username, role FROM users";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\nRegistered Users:");
            System.out.println("-----------------");
            while (rs.next()) {
                String username = rs.getString("username");
                String role = rs.getString("role");
                System.out.println(username + " - " + role);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving user list: " + e.getMessage());
        }
    }

    public void viewLogs() {
        String query = "SELECT username, action, timestamp FROM user_logs ORDER BY timestamp DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n--- User Activity Logs ---");
            while (rs.next()) {
                String user = rs.getString("username");
                String action = rs.getString("action");
                String time = rs.getString("timestamp");
                System.out.println(user + " | " + action + " | " + time);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving logs: " + e.getMessage());
        }
    }


    public boolean userExists(String username) {
        String query = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            return rs.next();  // returns true if user exists

        } catch (SQLException e) {
            System.out.println("Error checking user existence: " + e.getMessage());
            return false;
        }
    }
    public List<UserLog> getAllUserLogs() {
        List<UserLog> logList = new ArrayList<>();
        String query = "SELECT username, action, timestamp FROM user_logs ORDER BY timestamp DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                String action = rs.getString("action");
                // Assuming 'timestamp' column is stored as a compatible string or TIMESTAMP type
                // You might need to adjust rs.getTimestamp or parse the string explicitly
                LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                logList.add(new UserLog(username, action, timestamp));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving user activity logs for GUI: " + e.getMessage());
        }
        return logList;
    }

}