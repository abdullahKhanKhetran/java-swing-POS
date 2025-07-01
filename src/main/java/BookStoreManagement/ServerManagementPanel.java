package BookStoreManagement;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.Properties;

public class ServerManagementPanel extends JPanel {
    private JTextField urlField, usernameField, newUsernameField;
    private JPasswordField passwordField, newPasswordField;
    private JComboBox<String> roleCombo;
    private JButton saveButton, testButton, backButton;
    private MainFrame mainFrame;
    private JPanel inputPanel, buttonPanel;

    public ServerManagementPanel(MainFrame mainFrame, Database db) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        // Input Fields
        inputPanel.add(new JLabel("Database URL:"));
        urlField = new JTextField(loadConfig("db_url"));
        inputPanel.add(urlField);

        inputPanel.add(new JLabel("Database Username:"));
        usernameField = new JTextField(loadConfig("db_user"));
        inputPanel.add(usernameField);

        inputPanel.add(new JLabel("Database Password:"));
        passwordField = new JPasswordField(loadConfig("db_pass"));
        inputPanel.add(passwordField);

        inputPanel.add(new JLabel("New User Username:"));
        newUsernameField = new JTextField();
        inputPanel.add(newUsernameField);

        inputPanel.add(new JLabel("New User Password:"));
        newPasswordField = new JPasswordField();
        inputPanel.add(newPasswordField);

        inputPanel.add(new JLabel("User Role:"));
        roleCombo = new JComboBox<>(new String[]{"admin", "developer"});
        inputPanel.add(roleCombo);

        // Buttons
        testButton = new JButton("Test Connection");
        saveButton = new JButton("Save & Connect");
        backButton = new JButton("<- EXIT");

        buttonPanel.add(testButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(backButton);

        // Button Actions
        testButton.addActionListener(e -> testConnection());
        saveButton.addActionListener(e -> saveAndConnect());
        backButton.addActionListener(e -> mainFrame.showScreen("DeveloperPanel"));
    }

    private String loadConfig(String key) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
            return properties.getProperty(key, "");
        } catch (IOException e) {
            return "";
        }
    }

    private void saveConfig(String key, String value) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
        } catch (IOException ignored) {}

        properties.setProperty(key, value);

        try (FileOutputStream fos = new FileOutputStream("config.properties")) {
            properties.store(fos, "Database Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testConnection() {
        String url = urlField.getText().trim();
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            JOptionPane.showMessageDialog(this, "Connection Successful!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Connection Failed!\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAndConnect() {
        saveConfig("db_url", urlField.getText().trim());
        saveConfig("db_user", usernameField.getText().trim());
        saveConfig("db_pass", new String(passwordField.getPassword()));

        String newUser = newUsernameField.getText().trim();
        String newPass = new String(newPasswordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();

        if (!newUser.isEmpty() && !newPass.isEmpty()) {
            createNewUser(newUser, newPass, role);
        }

        JOptionPane.showMessageDialog(this, "Database settings saved. Restart required.");
    }

    private void createNewUser(String user, String pass, String role) {
        String dbUrl = loadConfig("db_url");
        String dbUser = loadConfig("db_user");
        String dbPass = loadConfig("db_pass");

        String checkQuery = "SELECT COUNT(*) FROM credentials WHERE username = ?";
        String insertQuery = "INSERT INTO credentials (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
            // Check if user already exists
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, user);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "User already exists!", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // Insert new user
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setString(1, user);
                stmt.setString(2, pass); // Consider hashing the password
                stmt.setString(3, role);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "New user added successfully!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to add user!\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
