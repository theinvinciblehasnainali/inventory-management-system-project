package com.inventory.gui;

import javax.swing.*;
import com.inventory.auth.UserManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegistrationPanel extends JPanel {
    private InventoryManagementApp parent;
    private UserManager userManager;

    private JTextField newUsernameField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<String> roleComboBox;
    private JButton registerButton;
    private JButton backToLoginButton;

    public RegistrationPanel(InventoryManagementApp parent, UserManager userManager) {
        this.parent = parent;
        this.userManager = userManager;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel titleLabel = new JLabel("Register New User");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 20, 10);
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // New Username Field
        JLabel newUsernameLabel = new JLabel("New Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(newUsernameLabel, gbc);
        newUsernameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(newUsernameField, gbc);

        JLabel newPasswordLabel = new JLabel("New Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(newPasswordLabel, gbc);
        newPasswordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(newPasswordField, gbc);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        add(confirmPasswordLabel, gbc);
        confirmPasswordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(confirmPasswordField, gbc);


        JLabel roleLabel = new JLabel("Select Role:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        add(roleLabel, gbc);

        String[] roles = {"Employee", "Admin"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setSelectedIndex(0);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        add(roleComboBox, gbc);

        registerButton = new JButton("Register");
        gbc.gridx = 0;
        gbc.gridy = 5; // Adjust gridy to account for the new component
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        gbc.anchor = GridBagConstraints.CENTER;
        add(registerButton, gbc);

        registerButton.addActionListener(e -> attemptRegistration());

        backToLoginButton = new JButton("Back to Login");
        backToLoginButton.setFont(new Font("Arial", Font.PLAIN, 12));
        backToLoginButton.setForeground(Color.BLUE);
        backToLoginButton.setBorderPainted(false);
        backToLoginButton.setContentAreaFilled(false);
        backToLoginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 10, 5);
        gbc.anchor = GridBagConstraints.CENTER;
        add(backToLoginButton, gbc);

        backToLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (parent != null) {
                    parent.showCard("Login");
                    resetFields();
                }
            }
        });
    }

    private void attemptRegistration() {
        String username = newUsernameField.getText().trim();
        String password = new String(newPasswordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();
        String selectedRole = (String) roleComboBox.getSelectedItem(); // Get the selected role

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!", "Registration Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userManager.registerUser(username, password, selectedRole)) {
            JOptionPane.showMessageDialog(this, "Registration Successful! You can now login as " + selectedRole + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
            resetFields();
            if (parent != null) {
                parent.showCard("Login");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed! Username might already exist.", "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void resetFields() {
        newUsernameField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
        roleComboBox.setSelectedIndex(0);
    }
}