package com.inventory.gui;

import com.inventory.auth.UserManager;
import com.inventory.core.User; // Import the User DTO
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UsersPanel extends JPanel {
    private InventoryManagementApp parent;
    private UserManager userManager;
    private DefaultTableModel tableModel;
    private JTable userTable;
    private JButton deleteUserButton;
    private String loggedInUsername; // Now set via a setter
    private String loggedInUserRole; // Now set via a setter

    // FIX START: Constructor simplified, user details set later
    public UsersPanel(InventoryManagementApp parent, UserManager userManager) {
        this.parent = parent;
        this.userManager = userManager;
        // loggedInUsername and loggedInUserRole will be null initially
        // They will be set by setLoggedInUserDetails() method after login

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(240, 248, 255));

        // --- North Panel: Title ---
        JLabel title = new JLabel("User Management", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // --- Center Panel: User Table ---
        String[] columnNames = {"ID", "Username", "Role"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Users are not editable directly from table
            }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setFont(new Font("Arial", Font.PLAIN, 14));
        userTable.setRowHeight(25);
        userTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);

        // Add selection listener to enable/disable delete button
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Ensures this runs only once per selection
                updateDeleteButtonState();
            }
        });

        // --- South Panel: Buttons ---
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        southPanel.setOpaque(false);

        deleteUserButton = new JButton("Delete User");
        deleteUserButton.setBackground(new Color(200, 70, 70)); // Red color for delete
        deleteUserButton.setForeground(Color.WHITE);
        deleteUserButton.setFocusPainted(false);
        deleteUserButton.setEnabled(false); // Disabled by default
        southPanel.add(deleteUserButton);

        add(southPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        deleteUserButton.addActionListener(e -> deleteSelectedUser());

        // Initial load of users (might be empty until user details are set)
        loadUsersIntoTable();
        // Initial button state (will be disabled until user details and role are set)
        updateDeleteButtonState();
    }
    // FIX END

    /**
     * NEW METHOD: Sets the logged-in user's details.
     * This should be called by DashboardPanel after successful login.
     */
    public void setLoggedInUserDetails(String username, String role) {
        this.loggedInUsername = username;
        this.loggedInUserRole = role;
        loadUsersIntoTable(); // Reload data with user context
        updateDeleteButtonState(); // Re-evaluate button state based on new user role
    }


    // This method will be called by DashboardPanel to update role if needed
    // Keep this as it's separate from setting the logged-in user for actions.
    public void setRole(String role) {
        // This method primarily dictates panel visibility/enabled state based on admin/employee
        // For delete logic, setLoggedInUserDetails takes precedence for the actual username/role used.
        this.loggedInUserRole = role; // Update the role, ensuring consistency
        deleteUserButton.setVisible("ADMIN".equalsIgnoreCase(role)); // Ensure button visibility
        updateDeleteButtonState(); // Re-evaluate button state
    }

    /**
     * Loads all users from the UserManager into the table.
     */
    public void loadUsersIntoTable() {
        tableModel.setRowCount(0); // Clear existing rows
        List<User> users = userManager.getAllUsers(); // Get User DTOs
        for (User user : users) {
            tableModel.addRow(new Object[]{user.getId(), user.getUsername(), user.getRole()});
        }
        updateDeleteButtonState(); // Update button state after table refresh
    }

    /**
     * Handles the deletion of the selected user.
     */
    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "No User Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String targetUsername = (String) tableModel.getValueAt(selectedRow, 1); // Username is in column 1

        // CRITICAL CHECK: Ensure loggedInUsername is not null before proceeding
        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Logged in user context not available. Please re-login.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete user: " + targetUsername + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Call UserManager's delete method
            boolean success = userManager.deleteUser(loggedInUsername, targetUsername);

            if (success) {
                JOptionPane.showMessageDialog(this, "User '" + targetUsername + "' deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUsersIntoTable(); // Refresh the table
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user '" + targetUsername + "'. This might be due to deleting your own admin account or the last admin. See console for more details.", "Deletion Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Updates the enabled state of the delete button based on user's role and table selection.
     */
    private void updateDeleteButtonState() {
        // Button is only enabled if user is ADMIN AND a row is selected
        boolean isRowSelected = userTable.getSelectedRow() != -1;
        boolean isAdmin = "ADMIN".equalsIgnoreCase(loggedInUserRole);
        deleteUserButton.setEnabled(isRowSelected && isAdmin);
        deleteUserButton.setVisible(isAdmin); // Keep button hidden if not admin
    }

    /**
     * Method to reset the panel when it becomes active (e.g., tab switched).
     */
    public void resetPanel() {
        // Only reload table if user context is available
        if (loggedInUsername != null && !loggedInUsername.isEmpty()) {
            loadUsersIntoTable();
        } else {
            // If user context not set, clear table until it is
            tableModel.setRowCount(0);
        }
        updateDeleteButtonState(); // Ensure button state is correct on reset
    }
}