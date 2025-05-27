package com.inventory.util;
import com.inventory.db.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Logger {
    public static void log(String username, String action){
        String query = "INSERT INTO user_logs (username, action) VALUES (?, ?)";

        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, action);
            stmt.executeUpdate();

        }
        catch(SQLException e){
            System.out.println("Failed to log user activity: " + e.getMessage());
        }
    }
}
