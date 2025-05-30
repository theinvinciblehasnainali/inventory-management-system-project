package com.inventory.gui;

import com.inventory.auth.UserManager;
import com.inventory.core.UserLog; // Import the UserLog DTO

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter; // For formatting LocalDateTime
import java.util.List;

public class UserLogsPanel extends JPanel {
    private InventoryManagementApp parent;
    private UserManager userManager;
    private DefaultTableModel tableModel;
    private JTable logTable;

    // We don't need loggedInUsername/Role here as this panel is just for display,
    // and access control is handled by DashboardPanel showing/hiding the tab.
    // However, including a setRole for consistency is good practice.
    private String loggedInUserRole;

    public UserLogsPanel(InventoryManagementApp parent, UserManager userManager) {
        this.parent = parent;
        this.userManager = userManager;
        this.loggedInUserRole = ""; // Default, will be set by DashboardPanel

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(240, 248, 255));

        // --- North Panel: Title ---
        JLabel title = new JLabel("User Activity Logs", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // --- Center Panel: Logs Table ---
        String[] columnNames = {"Username", "Action", "Timestamp"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Logs are not editable
            }
        };
        logTable = new JTable(tableModel);
        logTable.setFont(new Font("Arial", Font.PLAIN, 14));
        logTable.setRowHeight(25);
        logTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        JScrollPane scrollPane = new JScrollPane(logTable);
        add(scrollPane, BorderLayout.CENTER);

        // Initial load of logs
        loadUserLogsIntoTable();
    }

    /**
     * Sets the role of the logged-in user for this panel.
     * While this panel doesn't have role-specific functionality,
     * this method is kept for consistent panel management.
     */
    public void setRole(String role) {
        this.loggedInUserRole = role;
        // No specific UI changes based on role for this simple panel.
        // Visibility is handled by DashboardPanel.
    }

    /**
     * Loads all user logs from the UserManager into the table.
     */
    public void loadUserLogsIntoTable() {
        tableModel.setRowCount(0); // Clear existing rows
        List<UserLog> logs = userManager.getAllUserLogs(); // Get UserLog DTOs

        // Define a formatter for the timestamp to display nicely
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (UserLog log : logs) {
            tableModel.addRow(new Object[]{
                    log.getUsername(),
                    log.getAction(),
                    log.getTimestamp().format(formatter) // Format the LocalDateTime
            });
        }
    }

    /**
     * Method to reset the panel when it becomes active (e.g., tab switched).
     * For this panel, it simply reloads the logs.
     */
    public void resetPanel() {
        loadUserLogsIntoTable();
    }
}