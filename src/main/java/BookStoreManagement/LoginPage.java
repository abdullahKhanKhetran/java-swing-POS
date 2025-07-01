package BookStoreManagement;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class LoginPage extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;
    private Database db;
    private MainFrame mainFrame;
    
    // Instance variables for labels and button (to allow language updates)
    private JLabel titleLabel;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JButton loginButton;

    public LoginPage(MainFrame mainFrame, Database db) {
        this.mainFrame = mainFrame;
        this.db = db;
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        titleLabel = new JLabel();
        // Default to English font (Arial) for title; will be updated in applyLanguage()
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        usernameLabel = new JLabel();
        add(usernameLabel, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        passwordLabel = new JLabel();
        add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        messageLabel = new JLabel("");
        messageLabel.setForeground(Color.RED);
        add(messageLabel, gbc);

        gbc.gridy++;
        loginButton = new JButton();
        add(loginButton, gbc);
        
        loginButton.addActionListener(e -> authenticateUser());
        
        // Apply the current language settings immediately
        applyLanguage();
    }
    
    private void authenticateUser() {
        if (db.getConnection() == null || isConnectionClosed()) {
            System.out.println("🔄 Reconnecting to database...");
            db = new Database();  // Reinitialize connection
        }

        if (db.getConnection() != null) {
            System.out.println("✅ Connection running");
        } else {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please restart the application.");
            return;
        }

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText(getLocalizedText("Please enter username and password.", "براہ مہربانی یوزر نیم اور پاس ورڈ درج کریں۔"));
            return;
        }

        User user = db.authenticate(username, password);

        if (user != null) {
            if (user.getRole().equalsIgnoreCase("admin")) {
                mainFrame.showScreen("MainMenu");
            } else if (user.getRole().equalsIgnoreCase("developer")) {
                mainFrame.showScreen("DeveloperPanel");
            }
        } else {
            JOptionPane.showMessageDialog(null, getLocalizedText("Invalid username or password", "غلط یوزر نیم یا پاس ورڈ"));
        }
    }

    private boolean isConnectionClosed() {
        try {
            return db.getConnection() == null || db.getConnection().isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }
    
    // This method updates the UI text, fonts, and orientation based on the language.
    public void applyLanguage() {
        String lang = db.getLanguage();  // Assume this returns "English" or "Urdu"
        if (lang.equalsIgnoreCase("Urdu")) {
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            // Update labels and button texts to Urdu and set Noori font.
            titleLabel.setText("بک اسٹور میں لاگ ان کریں");
            titleLabel.setFont(new Font("Jameel Noori Nastaleeq", Font.BOLD, 18));
            usernameLabel.setText("یوزر نیم:");
            usernameLabel.setFont(new Font("Jameel Noori Nastaleeq", Font.BOLD, 16));
            passwordLabel.setText("پاس ورڈ:");
            passwordLabel.setFont(new Font("Jameel Noori Nastaleeq", Font.BOLD, 16));
            loginButton.setText("لاگ ان");
            loginButton.setFont(new Font("Jameel Noori Nastaleeq", Font.BOLD, 16));
        } else {
            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            // Update labels and button texts to English and set Arial.
            titleLabel.setText("Login to Bookstore");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            usernameLabel.setText("Username:");
            usernameLabel.setFont(new Font("Arial", Font.BOLD, 16));
            passwordLabel.setText("Password:");
            passwordLabel.setFont(new Font("Arial", Font.BOLD, 16));
            loginButton.setText("Login");
            loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        }
        revalidate();
        repaint();
    }
    
    // Helper method to return text based on the current language.
    private String getLocalizedText(String english, String urdu) {
        return db.getLanguage().equalsIgnoreCase("Urdu") ? urdu : english;
    }
}
