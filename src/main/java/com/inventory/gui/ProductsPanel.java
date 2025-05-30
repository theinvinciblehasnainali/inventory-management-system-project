package com.inventory.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import com.inventory.core.ProductManager;
import com.inventory.core.Product;
import com.inventory.core.CategoryManager;
import com.inventory.core.Category;

public class ProductsPanel extends JPanel {
    private InventoryManagementApp parent;
    private ProductManager productManager;
    private CategoryManager categoryManager;
    private JLabel roleSpecificLabel;

    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton searchButton;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private String currentRole;

    public ProductsPanel(InventoryManagementApp parent, ProductManager productManager, CategoryManager categoryManager) {
        this.parent = parent;
        this.productManager = productManager;
        this.categoryManager = categoryManager;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(240, 248, 255));

        JPanel northPanel = new JPanel(new BorderLayout(10, 10));
        northPanel.setOpaque(false);

        JLabel title = new JLabel("Products Management", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        northPanel.add(title, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        searchPanel.setOpaque(false);
        searchField = new JTextField(25);
        searchButton = new JButton("Search");
        searchPanel.add(new JLabel("Search Product:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        northPanel.add(searchPanel, BorderLayout.CENTER);

        add(northPanel, BorderLayout.NORTH);


        String[] columnNames = {"ID", "Name", "Category ID", "Quantity", "Price"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setFont(new Font("Arial", Font.PLAIN, 14));
        productTable.setRowHeight(25);
        productTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        southPanel.setOpaque(false);

        addButton = new JButton("Add New Product");
        updateButton = new JButton("Update Selected Product");
        deleteButton = new JButton("Delete Selected Product");

        // Set button styles
        addButton.setBackground(new Color(70, 150, 70));
        updateButton.setBackground(new Color(70, 100, 180));
        deleteButton.setBackground(new Color(180, 70, 70));
        addButton.setForeground(Color.WHITE);
        updateButton.setForeground(Color.WHITE);
        deleteButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        updateButton.setFocusPainted(false);
        deleteButton.setFocusPainted(false);

        southPanel.add(addButton);
        southPanel.add(updateButton);
        southPanel.add(deleteButton);

        add(southPanel, BorderLayout.SOUTH);

        // --- Role Specific Label (for debugging/info) ---
        roleSpecificLabel = new JLabel("Role: ", SwingConstants.CENTER);
        roleSpecificLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        // This label is for internal use or can be added to a debug panel

        // --- Add Action Listeners ---
        searchButton.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());

        addButton.addActionListener(e -> showProductDialog(null));
        updateButton.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow != -1) {
                // Retrieve data from table model
                int productId = (int) tableModel.getValueAt(selectedRow, 0);
                String name = (String) tableModel.getValueAt(selectedRow, 1);
                int categoryId = (int) tableModel.getValueAt(selectedRow, 2);
                int quantity = (int) tableModel.getValueAt(selectedRow, 3);
                double price = (double) tableModel.getValueAt(selectedRow, 4);

                // Create a Product object from selected row data
                Product productToUpdate = new Product(productId, name, categoryId, quantity, price);
                showProductDialog(productToUpdate);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a product to update.", "No Product Selected", JOptionPane.WARNING_MESSAGE);
            }
        });
        deleteButton.addActionListener(e -> deleteSelectedProduct());

        // Initial load of products
        loadProductsIntoTable(productManager.getAllProducts());
    }

    public void setRole(String role) {
        this.currentRole = role;
        roleSpecificLabel.setText("Role: " + role + ". This panel will adapt based on your permissions.");

        boolean isAdmin = "Admin".equalsIgnoreCase(role);

        addButton.setVisible(isAdmin);
        updateButton.setVisible(isAdmin);
        deleteButton.setVisible(isAdmin);
    }

    private void loadProductsIntoTable(List<Product> products) {
        tableModel.setRowCount(0);
        for (Product product : products) {
            tableModel.addRow(new Object[]{
                    product.getId(),
                    product.getName(),
                    product.getCategoryId(), // Display Category ID for now
                    product.getQuantity(),   // Changed from getStock()
                    product.getPrice()
            });
        }
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadProductsIntoTable(productManager.getAllProducts());
        } else {
            List<Product> searchResults = productManager.searchProducts(query);
            loadProductsIntoTable(searchResults);
        }
    }

    private void showProductDialog(Product product) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                product == null ? "Add New Product" : "Update Product",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(450, 350); // Slightly larger dialog
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField idField = new JTextField(product != null ? String.valueOf(product.getId()) : "Auto-Generated");
        idField.setEditable(false);
        JTextField nameField = new JTextField(product != null ? product.getName() : "");

        // --- New: JComboBox for Category Selection ---
        List<Category> categories = categoryManager.getAllCategories();
        // Create a DefaultComboBoxModel to hold Category objects
        DefaultComboBoxModel<Category> categoryComboBoxModel = new DefaultComboBoxModel<>();
        for (Category cat : categories) {
            categoryComboBoxModel.addElement(cat);
        }
        JComboBox<Category> categoryComboBox = new JComboBox<>(categoryComboBoxModel);

        // Set initial selection for update mode
        if (product != null) {
            Category currentCategory = categoryManager.getCategoryById(product.getCategoryId());
            if (currentCategory != null) {
                categoryComboBox.setSelectedItem(currentCategory);
            }
        }
        // --- End New: JComboBox for Category Selection ---

        JTextField quantityField = new JTextField(product != null ? String.valueOf(product.getQuantity()) : ""); // Changed from stockField
        JTextField priceField = new JTextField(product != null ? String.valueOf(product.getPrice()) : "");

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Product ID:"), gbc);
        gbc.gridx = 1; dialog.add(idField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; dialog.add(nameField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; dialog.add(categoryComboBox, gbc); // Add the JComboBox

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Quantity:"), gbc); // Changed label
        gbc.gridx = 1; dialog.add(quantityField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1; dialog.add(priceField, gbc);

        JButton saveButton = new JButton(product == null ? "Add Product" : "Save Changes");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                Category selectedCategory = (Category) categoryComboBox.getSelectedItem(); // Get selected Category object
                int categoryId = selectedCategory != null ? selectedCategory.getId() : -1; // Get ID from selected Category
                int quantity = Integer.parseInt(quantityField.getText().trim()); // Changed from stock
                double price = Double.parseDouble(priceField.getText().trim());

                if (name.isEmpty() || categoryId == -1) {
                    JOptionPane.showMessageDialog(dialog, "Name and Category must be selected.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (quantity < 0 || price < 0) {
                    JOptionPane.showMessageDialog(dialog, "Quantity and Price cannot be negative.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }


                if (product == null) {
                    if (productManager.addProduct(name, categoryId, quantity, price)) {
                        JOptionPane.showMessageDialog(dialog, "Product added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadProductsIntoTable(productManager.getAllProducts());
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to add product. Check category ID or name uniqueness.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else { // Update existing product
                    // Call your ProductManager's partialUpdateProduct method
                    boolean success = productManager.partialUpdateProduct(
                            product.getId(),
                            name,
                            categoryId,
                            quantity,
                            price
                    );
                    if (success) {
                        JOptionPane.showMessageDialog(dialog, "Product updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadProductsIntoTable(productManager.getAllProducts());
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to update product. Check category ID.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for Quantity and Price.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    private void deleteSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow != -1) {
            int productId = (int) tableModel.getValueAt(selectedRow, 0);
            String productName = (String) tableModel.getValueAt(selectedRow, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete product: " + productName + " (ID: " + productId + ")?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Call your ProductManager's deleteProductById method
                if (productManager.deleteProductById(productId)) {
                    JOptionPane.showMessageDialog(this, "Product deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadProductsIntoTable(productManager.getAllProducts());
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete product.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.", "No Product Selected", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void resetPanel() {
        searchField.setText("");
        loadProductsIntoTable(productManager.getAllProducts());
    }
}