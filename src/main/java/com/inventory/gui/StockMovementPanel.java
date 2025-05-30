package com.inventory.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Calendar;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import com.inventory.core.StockMovement;
import com.inventory.core.StockMovement.ChangeType;
import com.inventory.core.StockMovementManager;
import com.inventory.core.ProductManager;
import com.inventory.core.Product;

import com.inventory.exceptions.ProductNotFoundException;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

public class StockMovementPanel extends JPanel {
    private InventoryManagementApp parent;
    private StockMovementManager stockMovementManager;
    private ProductManager productManager;

    private JTable movementTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> productFilterComboBox;
    // FIX START: Changed typeFilterComboBox to hold String
    private JComboBox<String> typeFilterComboBox;
    // FIX END

    private JDatePickerImpl startDatePicker;
    private JDatePickerImpl endDatePicker;
    private JButton applyFilterButton;
    private JButton recordMovementButton;
    private String currentRole;

    public StockMovementPanel(InventoryManagementApp parent, StockMovementManager stockMovementManager, ProductManager productManager) {
        this.parent = parent;
        this.stockMovementManager = stockMovementManager;
        this.productManager = productManager;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(240, 248, 255));

        // --- North Panel: Title and Filters ---
        JPanel northPanel = new JPanel(new BorderLayout(10, 10));
        northPanel.setOpaque(false);

        JLabel title = new JLabel("Stock Movements Log", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        northPanel.add(title, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        filterPanel.setOpaque(false);

        // Product Filter
        filterPanel.add(new JLabel("Product:"));
        productFilterComboBox = new JComboBox<>();
        populateProductFilterComboBox();
        filterPanel.add(productFilterComboBox);

        // Change Type Filter
        filterPanel.add(new JLabel("Type:"));
        // FIX START: Initialize and populate typeFilterComboBox with Strings
        typeFilterComboBox = new JComboBox<>();
        typeFilterComboBox.addItem("All"); // Add "All" as a String
        for (ChangeType type : ChangeType.values()) {
            typeFilterComboBox.addItem(type.name()); // Add IN and OUT as their String names
        }
        typeFilterComboBox.setSelectedIndex(0); // Select "All" by default
        // FIX END
        filterPanel.add(typeFilterComboBox);

        // Date Range Filters (using JDatePicker)
        UtilDateModel model = new UtilDateModel();
        JDatePanelImpl datePanel = new JDatePanelImpl(model, new Properties());
        startDatePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        filterPanel.add(new JLabel("From:"));
        filterPanel.add(startDatePicker);

        UtilDateModel model2 = new UtilDateModel();
        JDatePanelImpl datePanel2 = new JDatePanelImpl(model2, new Properties());
        endDatePicker = new JDatePickerImpl(datePanel2, new DateLabelFormatter());
        filterPanel.add(new JLabel("To:"));
        filterPanel.add(endDatePicker);

        applyFilterButton = new JButton("Apply Filters");
        filterPanel.add(applyFilterButton);

        northPanel.add(filterPanel, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        // --- Center Panel: Movement Table ---
        String[] columnNames = {"ID", "Product Name", "Change Type", "Quantity", "Movement Time"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        movementTable = new JTable(tableModel);
        movementTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        movementTable.setFont(new Font("Arial", Font.PLAIN, 14));
        movementTable.setRowHeight(25);
        movementTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        JScrollPane scrollPane = new JScrollPane(movementTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- South Panel: Action Button ---
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        southPanel.setOpaque(false);

        recordMovementButton = new JButton("Record New Stock Movement");
        recordMovementButton.setBackground(new Color(70, 150, 70));
        recordMovementButton.setForeground(Color.WHITE);
        recordMovementButton.setFocusPainted(false);
        southPanel.add(recordMovementButton);

        add(southPanel, BorderLayout.SOUTH);

        // --- Add Action Listeners ---
        applyFilterButton.addActionListener(e -> applyFilters());
        recordMovementButton.addActionListener(e -> showRecordMovementDialog());

        // Initial load of movements
        loadStockMovementsIntoTable(stockMovementManager.getAllStockMovements());
    }

    private void populateProductFilterComboBox() {
        productFilterComboBox.removeAllItems();
        productFilterComboBox.addItem("All Products");
        List<Product> products = productManager.getAllProducts();
        for (Product product : products) {
            productFilterComboBox.addItem(product.getId() + " - " + product.getName());
        }
    }

    public void setRole(String role) {
        this.currentRole = role;
    }

    private void loadStockMovementsIntoTable(List<StockMovement> movements) {
        tableModel.setRowCount(0);
        for (StockMovement movement : movements) {
            String productName = "Unknown Product";
            Product product = null;
            try {
                product = productManager.getProductById(movement.getProductId());
            } catch (ProductNotFoundException e) {
                throw new RuntimeException(e);
            }
            if (product != null) {
                productName = product.getName();
            }

            tableModel.addRow(new Object[]{
                    movement.getId(),
                    productName,
                    movement.getChangeType(),
                    movement.getQuantity(),
                    movement.getMovementTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            });
        }
    }

    private void applyFilters() {
        List<StockMovement> filteredMovements = stockMovementManager.getAllStockMovements();

        // Filter by Product
        int selectedProductIndex = productFilterComboBox.getSelectedIndex();
        if (selectedProductIndex > 0) {
            String selectedProductText = (String) productFilterComboBox.getSelectedItem();
            int productId = Integer.parseInt(selectedProductText.split(" - ")[0]);
            filteredMovements = stockMovementManager.getMovementsByProductId(productId);
        }

        // FIX START: Handle String for Change Type filter
        String selectedChangeTypeStr = (String) typeFilterComboBox.getSelectedItem();
        if (selectedChangeTypeStr != null && !selectedChangeTypeStr.equals("All")) {
            ChangeType selectedChangeType = ChangeType.valueOf(selectedChangeTypeStr); // Now safe to convert
            filteredMovements = filteredMovements.stream()
                    .filter(m -> m.getChangeType() == selectedChangeType)
                    .collect(java.util.ArrayList::new, java.util.ArrayList::add, java.util.ArrayList::addAll);
        }
        // FIX END

        // Filter by Date Range
        Date startDate = (Date) startDatePicker.getModel().getValue();
        Date endDate = (Date) endDatePicker.getModel().getValue();

        LocalDateTime tempStartLDT = null;
        if (startDate != null) {
            tempStartLDT = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            tempStartLDT = tempStartLDT.withHour(0).withMinute(0).withSecond(0).withNano(0);
        }
        final LocalDateTime finalStartLDT = tempStartLDT;

        LocalDateTime tempEndLDT = null;
        if (endDate != null) {
            tempEndLDT = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            tempEndLDT = tempEndLDT.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        }
        final LocalDateTime finalEndLDT = tempEndLDT;

        if (finalStartLDT != null && finalEndLDT != null) {
            if (finalStartLDT.isAfter(finalEndLDT)) {
                JOptionPane.showMessageDialog(this, "Start date cannot be after end date.", "Date Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            filteredMovements = stockMovementManager.getMovementsBetweenDates(finalStartLDT, finalEndLDT);
            // Re-apply product and type filters to this date-filtered list
            if (selectedProductIndex > 0) {
                String selectedProductText = (String) productFilterComboBox.getSelectedItem();
                int productId = Integer.parseInt(selectedProductText.split(" - ")[0]);
                filteredMovements = filteredMovements.stream()
                        .filter(m -> m.getProductId() == productId)
                        .collect(java.util.ArrayList::new, java.util.ArrayList::add, java.util.ArrayList::addAll);
            }
            // FIX START: Use selectedChangeTypeStr for re-applying type filter
            if (selectedChangeTypeStr != null && !selectedChangeTypeStr.equals("All")) {
                ChangeType selectedChangeType = ChangeType.valueOf(selectedChangeTypeStr);
                filteredMovements = filteredMovements.stream()
                        .filter(m -> m.getChangeType() == selectedChangeType)
                        .collect(java.util.ArrayList::new, java.util.ArrayList::add, java.util.ArrayList::addAll);
            }
            // FIX END
        } else if (finalStartLDT != null) {
            filteredMovements = filteredMovements.stream()
                    .filter(m -> m.getMovementTime().isAfter(finalStartLDT) || m.getMovementTime().isEqual(finalStartLDT))
                    .collect(java.util.ArrayList::new, java.util.ArrayList::add, java.util.ArrayList::addAll);
        } else if (finalEndLDT != null) {
            filteredMovements = filteredMovements.stream()
                    .filter(m -> m.getMovementTime().isBefore(finalEndLDT) || m.getMovementTime().isEqual(finalEndLDT))
                    .collect(java.util.ArrayList::new, java.util.ArrayList::add, java.util.ArrayList::addAll);
        }

        loadStockMovementsIntoTable(filteredMovements);
    }

    private void showRecordMovementDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Record New Stock Movement", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<Product> products = productManager.getAllProducts();
        JComboBox<String> productComboBox = new JComboBox<>();
        for (Product p : products) {
            productComboBox.addItem(p.getId() + " - " + p.getName());
        }
        if (products.isEmpty()) {
            productComboBox.addItem("No Products Available");
            productComboBox.setEnabled(false);
        }

        JComboBox<ChangeType> changeTypeComboBox = new JComboBox<>(ChangeType.values());
        JTextField quantityField = new JTextField();

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Product:"), gbc);
        gbc.gridx = 1; dialog.add(productComboBox, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Movement Type:"), gbc);
        gbc.gridx = 1; dialog.add(changeTypeComboBox, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1; dialog.add(quantityField, gbc);

        JButton recordButton = new JButton("Record Movement");
        JButton cancelButton = new JButton("Cancel");

        recordButton.addActionListener(e -> {
            if (productComboBox.getSelectedItem() == null || productComboBox.getSelectedItem().equals("No Products Available")) {
                JOptionPane.showMessageDialog(dialog, "Please select a valid product.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                String selectedProductText = (String) productComboBox.getSelectedItem();
                int productId = Integer.parseInt(selectedProductText.split(" - ")[0]);
                ChangeType changeType = (ChangeType) changeTypeComboBox.getSelectedItem();
                int quantity = Integer.parseInt(quantityField.getText().trim());

                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Quantity must be greater than 0.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (changeType == ChangeType.OUT) {
                    Product currentProduct = productManager.getProductById(productId);
                    if (currentProduct == null) {
                        JOptionPane.showMessageDialog(dialog, "Product not found for stock check.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (currentProduct.getQuantity() < quantity) {
                        JOptionPane.showMessageDialog(dialog, "Not enough stock. Current: " + currentProduct.getQuantity(), "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

                if (stockMovementManager.updateProductStockAndLogMovement(productId, changeType, quantity)) {
                    JOptionPane.showMessageDialog(dialog, "Stock movement recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadStockMovementsIntoTable(stockMovementManager.getAllStockMovements());
                    if (parent.getDashboardPanel() != null && parent.getDashboardPanel().getProductsPanel() != null) {
                        parent.getDashboardPanel().getProductsPanel().resetPanel();
                    }
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to record stock movement. Product ID might be invalid or a database error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number for Quantity.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.add(recordButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    public void resetPanel() {
        productFilterComboBox.setSelectedIndex(0);
        typeFilterComboBox.setSelectedIndex(0); // This should now correctly select "All"
        startDatePicker.getModel().setValue(null);
        endDatePicker.getModel().setValue(null);
        populateProductFilterComboBox();
        loadStockMovementsIntoTable(stockMovementManager.getAllStockMovements());
    }

    private static class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private String datePattern = "yyyy-MM-dd";
        private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

        @Override
        public Object stringToValue(String text) {
            try {
                return dateFormatter.parseObject(text);
            } catch (ParseException e) {
                System.err.println("Error parsing date: " + text + " - " + e.getMessage());
                return null;
            }
        }

        @Override
        public String valueToString(Object value) {
            if (value != null) {
                Calendar cal = (Calendar) value;
                return dateFormatter.format(cal.getTime());
            }
            return "";
        }
    }
}