package BookStoreManagement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Database db;
    private LoginPage loginPage;
    private MainMenu mainMenu;
    private DeveloperPanel developerPanel;
    private ServerManagementPanel serverManagementPanel;

    // Top control panel for language toggle buttons
    private JPanel topControlPanel;
    private JButton englishButton;
    private JButton urduButton;
    
    // Bottom panel for clock display
    private JPanel bottomPanel;
    private JLabel clockLabel;
    private Timer clockTimer;

    public MainFrame() {
        db = new Database();
        setTitle("Bookstore Management");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize card layout and main panel
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialize application screens
        loginPage = new LoginPage(this, db);
        mainMenu = new MainMenu(this, db);
        developerPanel = new DeveloperPanel(this, db);
        serverManagementPanel = new ServerManagementPanel(this, db);

        // Add screens to the card layout panel
        mainPanel.add(loginPage, "LoginPage");
        mainPanel.add(mainMenu, "MainMenu");
        mainPanel.add(developerPanel, "DeveloperPanel");
        mainPanel.add(serverManagementPanel, "ServerManagementPanel");

        // Create top control panel with language toggle buttons
        topControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topControlPanel.setOpaque(false);
        englishButton = createTransparentButton("English");
        urduButton = createTransparentButton("اردو");
        englishButton.addActionListener(e -> switchLanguage("English"));
        urduButton.addActionListener(e -> switchLanguage("Urdu"));
        topControlPanel.add(englishButton);
        topControlPanel.add(urduButton);

        // Create bottom panel for clock display
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        clockLabel = new JLabel();
        clockLabel.setFont(new Font("Arial", Font.BOLD, 14));
        bottomPanel.add(clockLabel);

        // Arrange top, center, and bottom panels using BorderLayout
        setLayout(new BorderLayout());
        add(topControlPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        showScreen("LoginPage");
        startClock();
        setVisible(true);
    }

    // Switches the visible screen in the card layout
    public void showScreen(String screenName) {
        System.out.println("Switching to screen: " + screenName);
        cardLayout.show(mainPanel, screenName);
    }

    // Starts the clock timer to update the clock every second
    private void startClock() {
        clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();
    }

    // Updates the clock label with the current time
    private void updateClock() {
        String dateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
        clockLabel.setText(dateTime);
    }

    // Creates a transparent button with the specified text
    private JButton createTransparentButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 40));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        if (text.matches(".*[\\u0600-\\u06FF].*")) {
            button.setFont(new Font("Jameel Noori Nastaleeq", Font.BOLD, 18));
        } else {
            button.setFont(new Font("Arial", Font.BOLD, 18));
        }
        button.setForeground(Color.BLACK);
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setForeground(Color.BLUE);
            }
            public void mouseExited(MouseEvent evt) {
                button.setForeground(Color.BLACK);
            }
        });
        return button;
    }

    // Switches the language by updating the Database and relevant screens
    private void switchLanguage(String lang) {
        db.setLanguage(lang);
        System.out.println("Language switched to: " + lang);
        if (loginPage != null) {
            loginPage.applyLanguage();
            loginPage.revalidate();
            loginPage.repaint();
        }
        if (mainMenu != null) {
            mainMenu.setLanguage(lang);
            mainMenu.revalidate();
            mainMenu.repaint();
        }
        // Update other screens similarly if needed.
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new MainFrame().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "An unexpected error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
