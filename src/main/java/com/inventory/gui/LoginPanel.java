package com.inventory.gui;

import javax.swing.*;
import com.inventory.auth.UserManager;
import com.inventory.util.Logger;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {
    private UserManager userManager;
    private InventoryManagementApp parent;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private String usernameReturn;

    public LoginPanel(InventoryManagementApp parent, UserManager userManager) {
        this.parent = parent;
        this.userManager = userManager;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 20, 10);
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel userLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(userLabel, gbc);

        usernameField = new JTextField(15);
        gbc.gridx = 1;
        add(usernameField, gbc);

        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(passLabel, gbc);

        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        add(passwordField, gbc);

        loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        add(loginButton, gbc);

        loginButton.addActionListener(e -> attemptLogin());

        registerButton = new JButton("Register a New User");
        registerButton.setFont(new Font("Arial", Font.PLAIN, 12));
        registerButton.setForeground(Color.BLUE);
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 10, 5);
        add(registerButton, gbc);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (parent != null) {
                    parent.showCard("Registration");
                }
            }
        });
    }

    public void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (userManager.authenticate(username, password)) {
            String role = userManager.getUserRole(username);
            Logger.log(username, "LOGIN SUCCESS");
            JOptionPane.showMessageDialog(this, "Login Successful! Welcome " + username + "! Your role is: " + role);
            usernameReturn = username;
            if (parent != null) {
                parent.onLoginSuccess(username, role);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    public String returnUsername(){
        return usernameReturn;
    }

    public void resetFields() {
        usernameField.setText("");
        passwordField.setText("");
    }
}