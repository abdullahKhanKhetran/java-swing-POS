package BookStoreManagement;

import java.awt.Font;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class Database {
    private static Connection connection; // Shared connection
    private static String dbUrl;
    private static String dbUser;
    private static String dbPass;
    
    // New language field and default value
    private static String language = "English";

    public Database() {
        loadConfig();
        connect();
    }

    private void loadConfig() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
            dbUrl = properties.getProperty("db_url", "jdbc:mysql://localhost:3306/BookStore");
            dbUser = properties.getProperty("db_user", "bookstore_dev");
            dbPass = properties.getProperty("db_pass", "Abdullah@1");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load database configuration.");
        }
    }

    private void connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                System.out.println("✅ Database Connected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Database connection failed. Please check your connection settings and restart the app. " + e.getMessage());
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("🔄 Reconnecting to database...");
                connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Database Connection Closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User authenticate(String username, String password) {
        if (getConnection() == null) {
            System.out.println("❌ Connection closed.");
            return null;
        }

        System.out.println("✅ Connection running");
        String query = "SELECT username, password, role FROM credentials WHERE username = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getString("password").equals(password)) {
                return new User(username, rs.getString("password"), "DEV-KEY", "2025-01-01", "2026-01-01", rs.getString("role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateCredentials(String role, String username, String newPassword) {
        String query = "UPDATE credentials SET username = ?, password = ? WHERE role = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, newPassword);
            stmt.setString(3, role);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // New method to set the current language
    public void setLanguage(String lang) {
        language = lang;
        System.out.println("Language set to: " + language);
    }
    
    // New method to get the current language
    public String getLanguage() {
        return language;
    }
    
    public Font getJameelNooriFont(float size) {
        try (InputStream is = getClass().getResourceAsStream("/fonts/JameelNooriNastaleeq.ttf")) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            return font.deriveFont(Font.PLAIN, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            // Fallback to a default font if something goes wrong
            return new Font("Serif", Font.PLAIN, (int) size);
        }
    }
}
