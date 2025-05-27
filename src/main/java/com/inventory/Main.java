package com.inventory;
import com.inventory.core.*;
import com.inventory.auth.UserManager;
import com.inventory.exceptions.ProductNotFoundException;
import com.inventory.exceptions.StockMovementNotFoundException;
import com.inventory.util.Logger;
import com.inventory.core.StockMovement.ChangeType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        /*ProductManager productManager = new ProductManager();
        CategoryManager categoryManager = new CategoryManager();
        StockMovementManager stockMovementManager = new StockMovementManager();*/

        UserManager userManager = new UserManager();
        Scanner scanner = new Scanner(System.in);
        String loggedInUser = null;

        while (true) {
            System.out.println("\n1. Login\n2. Register\n3. Exit");
            System.out.print("Choose an option: ");
            String option = scanner.nextLine();
            switch (option) {
                case "1" -> login(scanner, userManager);
                case "2" -> register(scanner, userManager);
                case "3" -> {
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Invalid option");
            }

        }
    }
    private static void login(Scanner scanner, UserManager userManager) {
        System.out.println("\nEnter your username: ");
        String username = scanner.nextLine();
        System.out.println("\nEnter your password: ");
        String password = scanner.nextLine();
        if(userManager.authenticate(username, password)) {
            String role = userManager.getUserRole(username);
            System.out.println("\nYou are logged in successfully as: " + role);
            Logger.log(username, "LOGIN SUCCESS");
            if("ADMIN".equalsIgnoreCase(role)) {
                handleAdminMenu(scanner, username, role);
            }
            else{
                handleEmployeeMenu(scanner, username, role);
            }
        }
        else {
            System.out.println("\nInvalid username or password. Login failed.");
        }
    }
    private static void register(Scanner scanner, UserManager userManager) {
        System.out.print("Choose role (admin/employee): ");
        String roleInput = scanner.nextLine().trim().toUpperCase();

        // Validate role
        if (!roleInput.equals("ADMIN") && !roleInput.equals("EMPLOYEE")) {
            System.out.println("Invalid role. Please choose 'admin' or 'employee'.");
            return;
        }

        System.out.print("New username: ");
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }

        if (userManager.userExists(username)) {
            System.out.println("Username already exists.");
            return;
        }

        System.out.print("New password: ");
        String password = scanner.nextLine().trim();

        if (password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        boolean success = userManager.registerUser(username, password, roleInput);
        if (success) {
            System.out.println("User registered successfully.");
        } else {
            System.out.println("Registration failed.");
        }
    }
    public static void handleAdminMenu(Scanner scanner, String username, String role) {
        ProductManager productManager = new ProductManager();
        CategoryManager categoryManager = new CategoryManager();
        StockMovementManager stockMovementManager = new StockMovementManager();
        UserManager userManager = new UserManager();
        boolean backToMainMenu = false;
        while(!backToMainMenu){
            System.out.println("\n ----- ADMIN MENU -----\n");
            System.out.println("1. Manage Users");
            System.out.println("2. Manage Products");
            System.out.println("3. Manage Categories");
            System.out.println("4. Manage Stock Movements");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> handleUserMenu(scanner, userManager, username);
                case "2" -> handleProductMenu(scanner, productManager, role);
                case "3" -> handleCategoryMenu(scanner, categoryManager, role);
                case "4" -> handleStockMovementMenu(scanner,stockMovementManager);
                case "5" -> {
                    System.out.println("Logged out successfully.");
                    Logger.log(username, "LOGOUT");
                    backToMainMenu = true;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    public static void handleEmployeeMenu(Scanner scanner, String username, String role){
        ProductManager productManager = new ProductManager();
        CategoryManager categoryManager = new CategoryManager();
        StockMovementManager stockMovementManager = new StockMovementManager();
        UserManager userManager = new UserManager();
        boolean backToMainMenu = false;
        while(!backToMainMenu){
            System.out.println("\n ----- EMPLOYEE MENU -----\n");
            System.out.println("1. Manage Products");
            System.out.println("2. Manage Categories");
            System.out.println("3. Manage Stock Movements");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> handleProductMenu(scanner, productManager, role);
                case "2" -> handleCategoryMenu(scanner, categoryManager, role);
                case "3" -> handleStockMovementMenu(scanner, stockMovementManager);
                case "4" -> {
                    System.out.println("Logged out successfully.");
                    Logger.log(username, "LOGOUT");
                    backToMainMenu = true;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }
    public static void handleUserMenu(Scanner scanner, UserManager userManager, String username) {
        boolean backToMainMenu = false;
        while(!backToMainMenu) {
            System.out.println("\n--- USER MENU ---\n");
            System.out.println("1. List Users");
            System.out.println("2. Delete Users");
            System.out.println("3. View User Logs");
            System.out.println("4. Return to Admin Menu");
            System.out.println("Choose an option: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> userManager.listUsers();
                case "2" -> {
                    System.out.print("Enter username to delete: ");
                    String userToDelete = scanner.nextLine();
                    boolean deleted = userManager.deleteUser(username, userToDelete);
                    if (deleted) {
                        System.out.println("User '" + userToDelete + "' deleted successfully.");
                    } else {
                        System.out.println("Failed to delete user. Check rules or username.");
                    }
                }
                case "3" -> userManager.viewLogs();
                case "4" -> backToMainMenu = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }
    public static void handleProductMenu(Scanner scanner, ProductManager productManager, String role) {
        boolean back = false;

        while (!back) {
            System.out.println("\n--- PRODUCT MENU ---\n");
            System.out.println("1. List All Products");

            // Admin-only options
            if ("ADMIN".equalsIgnoreCase(role)) {
                System.out.println("2. Add New Product");
                System.out.println("3. Update Product");
                System.out.println("4. Delete Product");
                System.out.println("5. Search Product by Name");
                System.out.println("6. Search Product by ID");
                System.out.println("7. Back to Admin Menu");
            } else {
                // Employee has fewer options
                System.out.println("2. Search Product by Name");
                System.out.println("3. Back to Employee Menu");
            }

            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> System.out.println(productManager.getAllProducts());


                case "2" -> {
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        System.out.print("Enter product name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter category ID: ");
                        int categoryId = Integer.parseInt(scanner.nextLine());
                        System.out.print("Enter quantity: ");
                        int quantity = Integer.parseInt(scanner.nextLine());
                        System.out.print("Enter price: ");
                        double price = Double.parseDouble(scanner.nextLine());

                        boolean success = productManager.addProduct(name, categoryId, quantity, price);
                        System.out.println(success ? "Product added successfully." : "Failed to add product.");
                    } else {
                        System.out.print("Enter keyword to search: ");
                        String keyword = scanner.nextLine();
                        System.out.println(productManager.searchProducts(keyword));
                    }
                }

                case "3" -> {
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        System.out.print("Enter Product ID or Name to update: ");
                        if(scanner.hasNextInt()){
                            int productId = Integer.parseInt(scanner.next());
                            scanner.nextLine(); // Clear the rest of the line

                            try {

                                System.out.print("Enter new name (leave empty if no change): ");
                                String newName = scanner.nextLine().trim();

                                System.out.print("Enter new category ID (leave empty if no change): ");
                                String newCategoryId = scanner.nextLine().trim();

                                System.out.print("Enter new quantity (leave empty if no change): ");
                                String newQuantity = scanner.nextLine().trim();

                                System.out.print("Enter new price (leave empty if no change): ");
                                String newPrice = scanner.nextLine().trim();

                                // Parse fields or keep them null
                                Integer categoryId = newCategoryId.isEmpty() ? null : Integer.parseInt(newCategoryId);
                                Integer quantity = newQuantity.isEmpty() ? null : Integer.parseInt(newQuantity);
                                Double price = newPrice.isEmpty() ? null : Double.parseDouble(newPrice);
                                String name = newName.isEmpty() ? null : newName;

                                // Check if at least one field is being updated
                                if (name == null && categoryId == null && quantity == null && price == null) {
                                    System.out.println("No fields entered. Nothing to update.");
                                } else {
                                    boolean success = productManager.partialUpdateProduct(productId, name, categoryId, quantity, price);
                                    System.out.println(success ? "Product updated." : "Failed to update product.");
                                }

                            } catch (NumberFormatException e) {
                                System.out.println("Invalid input. Product ID, category ID, quantity, and price must be numbers.");
                            }
                        }
                        else if(scanner.hasNextLine()){
                            String productName = scanner.nextLine();
                            System.out.print("Enter new name (leave empty if no change): ");
                            String newName = scanner.nextLine().trim();

                            System.out.print("Enter new category ID (leave empty if no change): ");
                            String newCategoryId = scanner.nextLine().trim();

                            System.out.print("Enter new quantity (leave empty if no change): ");
                            String newQuantity = scanner.nextLine().trim();

                            System.out.print("Enter new price (leave empty if no change): ");
                            String newPrice = scanner.nextLine().trim();

                            try {
                                // Parse optional fields if provided
                                Integer categoryId = newCategoryId.isEmpty() ? null : Integer.parseInt(newCategoryId);
                                Integer quantity = newQuantity.isEmpty() ? null : Integer.parseInt(newQuantity);
                                Double price = newPrice.isEmpty() ? null : Double.parseDouble(newPrice);
                                String name = newName.isEmpty() ? null : newName;

                                // Ensure at least one field is provided for update
                                if (name == null && categoryId == null && quantity == null && price == null) {
                                    System.out.println("No update fields entered. Nothing to update.");
                                } else {
                                    boolean success = productManager.partialUpdateProduct(productName, name, categoryId, quantity, price);
                                    System.out.println(success ? "Product updated successfully." : "Failed to update product.");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid input. Category ID, quantity, and price must be numbers.");
                            }
                        }
                    } else {
                        back = true;  // Employee menu back option
                    }
                }

                case "4" -> {
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        System.out.println("Enter Product ID or Name to Delete Product: ");
                        if (scanner.hasNextInt()){
                            int productId = Integer.parseInt(scanner.nextLine());

                            boolean success = productManager.deleteProductById(productId);
                            System.out.println(success ? "Product deleted." : "Failed to delete product.");
                        } else if (scanner.hasNextLine()){
                            String productName = scanner.nextLine();
                            boolean success = productManager.deleteProductByName(productName);
                            System.out.println(success ? "Product deleted." : "Failed to delete product.");
                        }
                    } else {
                        System.out.println("Invalid option.");
                    }
                }

                case "5" -> {
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        System.out.print("Enter keyword to search: ");
                        String keyword = scanner.nextLine();
                        System.out.println(productManager.searchProducts(keyword));
                    } else {
                        System.out.println("Invalid option.");
                    }
                }

                case "6" -> {
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        System.out.print("Enter Product ID to search: ");
                        try {
                            int productId = Integer.parseInt(scanner.nextLine().trim());

                            try {
                                System.out.println(productManager.getProductById(productId));
                            } catch (ProductNotFoundException e) {
                                System.out.println("Error while searching product: " + e.getMessage());
                            }

                        } catch (NumberFormatException e) {
                            System.out.println("Invalid Product ID. Please enter a valid number.");
                        }
                    }
                }

                case "7" -> {
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        back = true; // Admin back
                    } else {
                        System.out.println("Invalid option.");
                    }
                }

                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }
    public static void handleCategoryMenu(Scanner scanner, CategoryManager categoryManager, String role) {
        boolean back = false;
        while(!back){
            System.out.println("\n--- CATEGORY MENU ---\n");
            System.out.println("1. View All Categories");
            //Admin-only options
            if("ADMIN".equalsIgnoreCase(role)){
                System.out.println("2. Add New Category");
                System.out.println("3. Update Category");
                System.out.println("4. Delete Category");
                System.out.println("5. Search Category by Name");
                System.out.println("6. Search Category by Category ID");
                System.out.println("7. Back to Admin Menu");
            }
            else{
                System.out.println("2. Search Category by Name");
                System.out.println("3. Search Category by Category ID");
                System.out.println("4. Back to Employee Menu");
            }
            System.out.println("Enter your choice: ");
            String choice = scanner.nextLine();
            switch(choice){
                case "1" -> System.out.println(categoryManager.getAllCategories());
                case "2" -> {
                    if("ADMIN".equalsIgnoreCase(role)){
                        System.out.print("Enter category name: ");
                        String categoryName = scanner.nextLine();
                        boolean success = categoryManager.addCategory(categoryName);
                        System.out.println(success ? "Added category successfully." : "Failed to add category.");
                    }
                    else{
                        System.out.println("Enter a keyword to search: ");
                        String keyword = scanner.nextLine();
                        System.out.println(categoryManager.searchCategories(keyword));
                    }
                }
                case "3" -> {
                    if("ADMIN".equalsIgnoreCase(role)){
                        System.out.println("Enter category Name or ID to update: ");
                        if(scanner.hasNextInt()){
                            int categoryId = Integer.parseInt(scanner.nextLine());
                            System.out.println("Enter the new category name: ");
                            String newCategoryName = scanner.nextLine();
                            boolean success = categoryManager.updateCategoryName(categoryId, newCategoryName);
                            System.out.println(success ? "Category updated successfully." : "Failed to update category.");
                        }
                        else if(scanner.hasNextLine()){
                            String categoryName = scanner.nextLine();
                            System.out.println("Enter the new category name: ");
                            String newCategoryName = scanner.nextLine();
                            boolean success = categoryManager.updateCategoryName(categoryName, newCategoryName);
                            System.out.println(success ? "Category updated successfully." : "Failed to update category.");
                        }
                    }
                    else{
                        System.out.println("Enter category ID to search: ");
                        try{
                        int categoryId = Integer.parseInt(scanner.nextLine());
                        System.out.println(categoryManager.getCategoryById(categoryId));}
                        catch(NumberFormatException e){
                            System.out.println("Invalid category ID. Please enter a valid number.");
                        }
                    }
                }
                case "4" -> {
                    if("ADMIN".equalsIgnoreCase(role)){
                        System.out.println("Enter category ID or Name to delete: ");
                        if(scanner.hasNextInt()){
                            int categoryId = Integer.parseInt(scanner.nextLine());
                            boolean success = categoryManager.deleteCategoryByID(categoryId);
                        }
                        else if(scanner.hasNextLine()){
                            String categoryName = scanner.nextLine();
                            boolean success = categoryManager.deleteCategoryByName(categoryName);
                            System.out.println(success ? "Category deleted successfully." : "Failed to delete category.");
                        }
                    }
                    else{
                        back = true; // Employee main menu
                    }
                }
                case "5" -> {
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        System.out.println("Enter a keyword to search: ");
                        String keyword = scanner.nextLine();
                        System.out.println(categoryManager.searchCategories(keyword));
                    }
                    else{
                        System.out.println("Invalid option.");
                    }
                }
                case "6" -> {
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        System.out.println("Enter category ID to search: ");
                        try{
                        int categoryId = Integer.parseInt(scanner.nextLine());
                        System.out.println(categoryManager.getCategoryById(categoryId));}
                        catch(NumberFormatException e){
                            System.out.println("Invalid category ID. Please enter a valid number.");
                        }
                    }
                    else {
                        System.out.println("Invalid option.");
                    }
                }
                case "7" -> {
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        back = true; // Admin main menu
                    }
                    else {
                        System.out.println("Invalid option.");
                    }
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }
    public static void handleStockMovementMenu(Scanner scanner, StockMovementManager stockMovementManager) {
        boolean back = false;
        while(!back){
            System.out.println("\n--- STOCK MOVEMENT MENU ---\n");
            System.out.println("1. List All Stock Movements");
            System.out.println("2. Update Stock Movements");
            System.out.println("3. Get Stock Movement between dates");
            System.out.println("4. Search Stock Movements by Product ID");
            System.out.println("5. Update Product Stock and Log Movement");
            System.out.println("6. Get Latest Movements for a Product");
            System.out.println("7. Filter Stock Movements by Type");
            System.out.println("8. Back to Menu");
            System.out.println("Enter your choice: ");
            String choice = scanner.nextLine();
            switch(choice){
                case "1" -> System.out.println(stockMovementManager.getAllStockMovements());
                case "2" -> {
                    System.out.println("Enter Product ID: ");
                    int productId = Integer.parseInt(scanner.nextLine());
                    ChangeType changeType;
                    System.out.println("Enter Stock Movement Type(IN/OUT): ");
                    String movementType = scanner.nextLine().trim().toUpperCase();
                    try{
                        changeType = ChangeType.valueOf(movementType.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid change Type. Must be 'IN' or 'OUT'.");
                        break;
                    }
                    System.out.println("Enter Quantity: ");
                    String quantityInput = scanner.nextLine().trim();
                    int quantity;
                    try{
                        quantity = Integer.parseInt(quantityInput);
                    }
                    catch(NumberFormatException e){
                        System.out.println("Invalid quantity. Please enter a valid number.");
                        break;
                    }
                    boolean success = stockMovementManager.addStockMovement(productId, changeType, quantity);
                    System.out.println(success ? "Stock movement recorded successfully." : "Failed to record stock movement.");

                }
                case "3" -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                    System.out.print("Enter start date and time (yyyy-MM-dd HH:mm): ");
                    String startInput = scanner.nextLine();
                    System.out.print("Enter end date and time (yyyy-MM-dd HH:mm): ");
                    String endInput = scanner.nextLine();

                    try {
                        LocalDateTime start = LocalDateTime.parse(startInput, formatter);
                        LocalDateTime end = LocalDateTime.parse(endInput, formatter);

                        List<StockMovement> movements = stockMovementManager.getMovementsBetweenDates(start, end);

                        if (movements.isEmpty()) {
                            System.out.println("No stock movements found in this range.");
                        } else {
                            for (StockMovement movement : movements) {
                                System.out.println(movement);
                            }
                        }

                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date/time format. Please use yyyy-MM-dd HH:mm.");
                    }

                }
                case "4" -> {
                    System.out.println("Enter Product ID to search: ");
                    try{
                    int productId = Integer.parseInt(scanner.nextLine());
                    System.out.println(stockMovementManager.getMovementsByProductId(productId));}
                    catch(NumberFormatException e){
                        System.out.println("Invalid product ID. Please enter a valid number.");
                    }
                }
                case "5" -> {
                    System.out.println("Enter Product ID: ");
                    int productId;
                    try{
                        productId = Integer.parseInt(scanner.nextLine());
                    }
                    catch(NumberFormatException e){
                        System.out.println("Invalid product ID. Please enter a valid number.");
                        break;
                    }
                    ChangeType changeType;
                    System.out.println("Enter change Type (IN/OUT): ");
                    String changeTypeInput = scanner.nextLine().trim().toUpperCase();
                    try{
                        changeType = ChangeType.valueOf(changeTypeInput.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid change Type. Please use 'IN' or 'OUT'.");
                        break;
                    }
                    System.out.println("Enter Quantity: ");
                    int quantity;
                    try{
                        quantity = Integer.parseInt(scanner.nextLine());
                    }
                    catch(NumberFormatException e){
                        System.out.println("Invalid quantity. Please enter a valid number.");
                        break;
                    }
                    boolean success = stockMovementManager.updateProductStockAndLogMovement(productId, changeType, quantity);
                    System.out.println(success ? "Stock updated and movements logged successfully" :
                            "Failed to update stock or log movement.");
                }
                case "6" -> {
                    System.out.println("Enter Product ID: ");
                    int productId;
                    try{
                        productId = Integer.parseInt(scanner.nextLine());}
                    catch(NumberFormatException e){
                        System.out.println("Invalid Product ID. Please enter a valid number.");
                        break;
                    }
                    try {
                        StockMovement latestMovement = stockMovementManager.getLatestMovementForProduct(productId);
                        System.out.println("Latest Stock Movement:");
                        System.out.println(latestMovement);
                    } catch (StockMovementNotFoundException e) {
                        System.out.println("No stock movement found: " + e.getMessage());
                    }
                }
                case "7" -> {
                    System.out.println("Enter change Type (IN/OUT): ");
                    String changeType = scanner.nextLine().trim().toUpperCase();
                    if (!changeType.equals("IN") && !changeType.equals("OUT")) {
                        System.out.println("Invalid type. Must be IN or OUT.");
                    } else {
                        List<StockMovement> filtered = stockMovementManager.getMovementsByType(changeType);
                        if (filtered.isEmpty()) {
                            System.out.println("No stock movements found for type: " + changeType);
                        } else {
                            System.out.println("Stock Movements of type " + changeType + ":");
                            for (StockMovement movement : filtered) {
                                System.out.println(movement);
                            }
                        }
                    }
                }
                case "8" -> back = true;
                default -> System.out.println("Invalid option. Please try again.");
            }

        }
    }
}
