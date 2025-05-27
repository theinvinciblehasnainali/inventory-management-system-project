package com.inventory;
import javax.swing.*;

public class InventoryGUI extends JFrame {

    public InventoryGUI() {
        setTitle("Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // Center the window

        JTabbedPane tabbedPane = new JTabbedPane();

        // Placeholder panels
        JPanel productsPanel = new JPanel();
        productsPanel.add(new JLabel("Products Table Here"));

        JPanel categoriesPanel = new JPanel();
        categoriesPanel.add(new JLabel("Categories Table Here"));

        JPanel stockMovementsPanel = new JPanel();
        stockMovementsPanel.add(new JLabel("Stock Movements Table Here"));

        // Add tabs
        tabbedPane.addTab("Products", productsPanel);
        tabbedPane.addTab("Categories", categoriesPanel);
        tabbedPane.addTab("Stock Movements", stockMovementsPanel);

        add(tabbedPane);
        setVisible(true);
    }
}
