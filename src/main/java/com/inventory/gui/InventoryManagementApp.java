package com.inventory.gui;

import javax.swing.*;
import java.awt.*;
import com.inventory.auth.UserManager;
import com.inventory.core.ProductManager;
import com.inventory.core.CategoryManager;
import com.inventory.core.StockMovementManager;
import com.inventory.util.Logger;


public class InventoryManagementApp extends JFrame {

    final private CardLayout cardLayout;
    final private JPanel mainPanel;
    final private LoginPanel loginPanel;
    final private DashboardPanel dashboardPanel;
    final private RegistrationPanel registrationPanel;

    private UserManager userManager;
    private ProductManager productManager;
    private CategoryManager categoryManager;
    private StockMovementManager stockMovementManager;


    public InventoryManagementApp() {
        setTitle("Inventory Management System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        userManager = new UserManager();
        productManager = new ProductManager();
        categoryManager = new CategoryManager();
        stockMovementManager = new StockMovementManager();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        loginPanel = new LoginPanel(this, userManager);
        registrationPanel = new RegistrationPanel(this, userManager);
        dashboardPanel = new DashboardPanel(this, productManager, categoryManager,
                stockMovementManager, userManager);


        mainPanel.add(loginPanel, "Login");
        mainPanel.add(dashboardPanel, "Dashboard");
        mainPanel.add(registrationPanel, "Registration");

        add(mainPanel);

        showCard("Login");
    }

    public void showCard(String cardName) {
        cardLayout.show(mainPanel, cardName);
        if ("Login".equals(cardName)) {
            loginPanel.resetFields();
        }
        if ("Registration".equals(cardName)) {
            registrationPanel.resetFields();
        }
    }

    public void onLoginSuccess(String username, String role) {
        dashboardPanel.setUserDetails(username, role);
        showCard("Dashboard");
    }

    public void logout(){
        Logger.log(loginPanel.returnUsername(), "LOGOUT");
        showCard("Login");
        dashboardPanel.resetDashboard();
    }
    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new InventoryManagementApp().setVisible(true);
        });
    }
}