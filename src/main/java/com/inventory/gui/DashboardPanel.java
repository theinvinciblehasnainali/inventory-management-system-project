package com.inventory.gui;
import com.inventory.core.*;
import com.inventory.auth.UserManager;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {
    private InventoryManagementApp parent;
    private JLabel welcomeLabel;
    private JTabbedPane tabbedPane;
    private ProductManager productManager;
    private CategoryManager categoryManager;
    private UserManager userManager;
    private StockMovementManager stockMovementManager;
    private ProductsPanel productsPanel;
    private CategoriesPanel categoriesPanel;
    private StockMovementPanel stockMovementPanel;
    private UsersPanel usersPanel;
    private UserLogsPanel userLogsPanel;
    private JLabel userRoleLabel;
    private LoginPanel loginPanel;
    private String currentLoggedInUsername;
    private String currentLoggedInUserRole;

    public DashboardPanel(InventoryManagementApp parent, ProductManager productManager,
                          CategoryManager categoryManager,
                          StockMovementManager stockMovementManager,
                          UserManager userManager) {
        this.parent = parent;
        this.productManager = productManager;
        this.categoryManager = categoryManager;
        this.stockMovementManager = stockMovementManager;
        this.userManager = userManager;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        headerPanel.setBackground(new Color(60, 90, 120)); // Darker header background
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        welcomeLabel = new JLabel("Welcome!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 22));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel);

        userRoleLabel = new JLabel("Role: ");
        userRoleLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        userRoleLabel.setForeground(Color.WHITE);
        headerPanel.add(userRoleLabel);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 14));
        logoutButton.setBackground(new Color(200, 70, 70));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> {
            if (parent != null) {
                parent.logout();
            }
        });
        headerPanel.add(logoutButton);

        add(headerPanel, BorderLayout.NORTH);
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        add(tabbedPane, BorderLayout.CENTER);
        productsPanel = new ProductsPanel(parent, productManager, categoryManager);
        categoriesPanel = new CategoriesPanel(parent, categoryManager);
        stockMovementPanel = new StockMovementPanel(parent, stockMovementManager,productManager);
        usersPanel = new UsersPanel(parent, userManager);
        userLogsPanel = new UserLogsPanel(parent, userManager);
         /* @param username The logged-in user's username.
                * @param role The logged-in user's role ("Admin" or "Employee").
                */
        JTextArea dashboardContent = new JTextArea("This is your dashboard content.\nManage your inventory here!");
        dashboardContent.setEditable(false);
        dashboardContent.setLineWrap(true);
        dashboardContent.setWrapStyleWord(true);
    }

    // Method to set the user role on the dashboard
    public void setUserDetails(String username, String role) {
        this.currentLoggedInUsername = username;
        this.currentLoggedInUserRole = role;
        welcomeLabel.setText("Welcome, " + username + "!");
        if (role != null && !role.isEmpty()) {
            userRoleLabel.setText("Your role: " + role);
        } else {
            userRoleLabel.setText("Welcome! Role: Not Available");
        }
        tabbedPane.removeAll();
        if (usersPanel != null) {
            usersPanel.setLoggedInUserDetails(username, role);
        }
        if ("Admin".equalsIgnoreCase(role)) {
            addAdminTabs();
        } else if ("Employee".equalsIgnoreCase(role)) {
            addEmployeeTabs();
        } else {
            tabbedPane.addTab("Access Denied", new JLabel("Your role does not have access to this system."));
        }
    }
    private void addAdminTabs() {
        tabbedPane.addTab("Products", productsPanel);
        tabbedPane.addTab("Categories", categoriesPanel);
        tabbedPane.addTab("Stock Movements", stockMovementPanel);
        tabbedPane.addTab("Users", usersPanel);
        tabbedPane.addTab("User Logs", userLogsPanel);
        productsPanel.setRole("Admin");
        categoriesPanel.setRole("Admin");
        stockMovementPanel.setRole("Admin");
        usersPanel.setRole("Admin");
        userLogsPanel.setRole("Admin");
    }
    private void addEmployeeTabs() {
        tabbedPane.addTab("Products", productsPanel);
        tabbedPane.addTab("Categories", categoriesPanel);
        tabbedPane.addTab("Stock Movements", stockMovementPanel);
        productsPanel.setRole("Employee");
        categoriesPanel.setRole("Employee");
        stockMovementPanel.setRole("Employee");
    }
    public void resetDashboard() {
        welcomeLabel.setText("Welcome!");
        userRoleLabel.setText("Your role: ");
        tabbedPane.removeAll();
        productsPanel.resetPanel();
        categoriesPanel.resetPanel();
        stockMovementPanel.resetPanel();
        usersPanel.resetPanel();
        userLogsPanel.resetPanel();
    }

    public ProductsPanel getProductsPanel() {
        return productsPanel;
    }
}