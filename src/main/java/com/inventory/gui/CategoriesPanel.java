package com.inventory.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import com.inventory.core.CategoryManager; // Your CategoryManager
import com.inventory.core.Category;       // Your Category model
import com.inventory.core.ProductManager;

public class CategoriesPanel extends JPanel {
    private InventoryManagementApp parent;
    private CategoryManager categoryManager;
    private ProductManager productManager;
    private JTable categoryTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton searchButton;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private String currentRole; // To keep track of the current user's role

    public CategoriesPanel(InventoryManagementApp parent, CategoryManager categoryManager) {
        this.parent = parent;
        this.categoryManager = categoryManager;
        setLayout(new BorderLayout(10, 10)); // Add some gaps
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(240, 248, 255)); // Light blue background

        // --- North Panel: Title and Search ---
        JPanel northPanel = new JPanel(new BorderLayout(10, 10));
        northPanel.setOpaque(false); // Make it transparent to show parent background

        JLabel title = new JLabel("Categories Management", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Padding below title
        northPanel.add(title, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        searchPanel.setOpaque(false);
        searchField = new JTextField(25);
        searchButton = new JButton("Search");
        searchPanel.add(new JLabel("Search Category:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        northPanel.add(searchPanel, BorderLayout.CENTER);

        add(northPanel, BorderLayout.NORTH);

        // --- Center Panel: Category Table ---
        String[] columnNames = {"ID", "Name"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };
        categoryTable = new JTable(tableModel);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Allow only one row selection
        categoryTable.setFont(new Font("Arial", Font.PLAIN, 14));
        categoryTable.setRowHeight(25);
        categoryTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        JScrollPane scrollPane = new JScrollPane(categoryTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- South Panel: Action Buttons ---
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)); // More space between buttons
        southPanel.setOpaque(false);

        addButton = new JButton("Add New Category");
        updateButton = new JButton("Update Selected Category");
        deleteButton = new JButton("Delete Selected Category");

        // Set button styles
        addButton.setBackground(new Color(70, 150, 70)); // Green
        updateButton.setBackground(new Color(70, 100, 180)); // Blue
        deleteButton.setBackground(new Color(180, 70, 70)); // Red
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

        // --- Add Action Listeners ---
        searchButton.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch()); // Search on Enter key in field

        addButton.addActionListener(e -> showCategoryDialog(null)); // null for new category
        updateButton.addActionListener(e -> {
            int selectedRow = categoryTable.getSelectedRow();
            if (selectedRow != -1) {
                int categoryId = (int) tableModel.getValueAt(selectedRow, 0);
                String name = (String) tableModel.getValueAt(selectedRow, 1);
                Category categoryToUpdate = new Category(categoryId, name);
                showCategoryDialog(categoryToUpdate);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a category to update.", "No Category Selected", JOptionPane.WARNING_MESSAGE);
            }
        });
        deleteButton.addActionListener(e -> deleteSelectedCategory());

        // Initial load of categories
        loadCategoriesIntoTable(categoryManager.getAllCategories());
    }

    /**
     * Sets the current user's role and adjusts UI elements accordingly.
     * This method is called by DashboardPanel.
     * @param role The role of the logged-in user ("Admin" or "Employee").
     */
    public void setRole(String role) {
        this.currentRole = role;
        boolean isAdmin = "Admin".equalsIgnoreCase(role);

        // Enable/disable CRUD buttons based on role
        addButton.setVisible(isAdmin);
        updateButton.setVisible(isAdmin);
        deleteButton.setVisible(isAdmin);
    }

    /**
     * Loads a list of categories into the JTable.
     * @param categories The list of Category objects to display.
     */
    private void loadCategoriesIntoTable(List<Category> categories) {
        tableModel.setRowCount(0); // Clear existing rows
        for (Category category : categories) {
            tableModel.addRow(new Object[]{
                    category.getId(),
                    category.getName()
            });
        }
    }

    /**
     * Performs a search based on the text in the search field.
     */
    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadCategoriesIntoTable(categoryManager.getAllCategories()); // Show all if search is empty
        } else {
            List<Category> searchResults = categoryManager.searchCategories(query);
            loadCategoriesIntoTable(searchResults);
        }
    }

    /**
     * Shows a dialog for adding a new category or updating an existing one.
     * @param category The category to update, or null if adding a new category.
     */
    private void showCategoryDialog(Category category) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                category == null ? "Add New Category" : "Update Category",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(350, 200); // Smaller dialog for categories
        dialog.setLocationRelativeTo(this); // Center relative to CategoriesPanel

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField idField = new JTextField(category != null ? String.valueOf(category.getId()) : "Auto-Generated");
        idField.setEditable(false); // ID is not editable by user
        JTextField nameField = new JTextField(category != null ? category.getName() : "");

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Category ID:"), gbc);
        gbc.gridx = 1; dialog.add(idField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; dialog.add(nameField, gbc);

        JButton saveButton = new JButton(category == null ? "Add Category" : "Save Changes");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Category Name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (category == null) { // Add new category
                if (categoryManager.addCategory(name)) {
                    JOptionPane.showMessageDialog(dialog, "Category added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCategoriesIntoTable(categoryManager.getAllCategories()); // Refresh table
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add category. Name might already exist or a database error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else { // Update existing category
                // Use updateCategoryName by ID for consistency and reliability
                if (categoryManager.updateCategoryName(category.getId(), name)) {
                    JOptionPane.showMessageDialog(dialog, "Category updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCategoriesIntoTable(categoryManager.getAllCategories()); // Refresh table
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update category. Database error or name conflict.", "Error", JOptionPane.ERROR_MESSAGE);
                }
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

    /**
     * Deletes the currently selected category from the table.
     */
    private void deleteSelectedCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow != -1) {
            int categoryId = (int) tableModel.getValueAt(selectedRow, 0);
            String categoryName = (String) tableModel.getValueAt(selectedRow, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete category: " + categoryName + " (ID: " + categoryId + ")?\n" +
                            "This might affect products linked to this category.",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (categoryManager.deleteCategoryByID(categoryId)) {
                    JOptionPane.showMessageDialog(this, "Category deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCategoriesIntoTable(categoryManager.getAllCategories()); // Refresh table
                    // Consider refreshing ProductsPanel if it's visible, as categories might have changed
                    // parent.getDashboardPanel().getProductsPanel().loadProductsIntoTable(parent.getDashboardPanel().getProductsPanel().getProductManager().getAllProducts());
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete category. It might be in use by products.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a category to delete.", "No Category Selected", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Resets the panel to its initial state, typically called on logout.
     */
    public void resetPanel() {
        searchField.setText("");
        loadCategoriesIntoTable(categoryManager.getAllCategories()); // Reload all categories
    }
}