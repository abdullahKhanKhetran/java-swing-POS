package BookStoreManagement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DeveloperPanel extends JPanel {
    private Database db;
    private JTextField adminUsernameField, devUsernameField;
    private JPasswordField adminPasswordField, devPasswordField;
    private JButton updateAdminButton, updateDevButton, logoutButton;
    private MainFrame mainFrame;
    public DeveloperPanel(MainFrame mainFrame, Database db) {
        this.db = db;
//        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Developer Panel");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        // Admin Credentials
        gbc.gridwidth = 1;
        gbc.gridy++;
        add(new JLabel("Admin Username:"), gbc);
        gbc.gridx = 1;
        adminUsernameField = new JTextField(15);
        add(adminUsernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("Admin Password:"), gbc);
        gbc.gridx = 1;
        adminPasswordField = new JPasswordField(15);
        add(adminPasswordField, gbc);

        updateAdminButton = new JButton("Update Admin Credentials");
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        add(updateAdminButton, gbc);

        // Developer Credentials
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        add(new JLabel("Developer Username:"), gbc);
        gbc.gridx = 1;
        devUsernameField = new JTextField(15);
        add(devUsernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("Developer Password:"), gbc);
        gbc.gridx = 1;
        devPasswordField = new JPasswordField(15);
        add(devPasswordField, gbc);

        updateDevButton = new JButton("Update Developer Credentials");
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        add(updateDevButton, gbc);

        // Logout Button
        logoutButton = new JButton("Logout");
        gbc.gridy++;
        add(logoutButton, gbc);
        
        gbc.gridy++;
        JButton databaseManagementButton = new JButton("server details");
        add(databaseManagementButton, gbc);
        
       

        // Button Actions
        databaseManagementButton.addActionListener(e -> mainFrame.showScreen("ServerManagementPanel") );
        updateAdminButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCredentials("admin", adminUsernameField.getText(), new String(adminPasswordField.getPassword()));
            }
        });

        updateDevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCredentials("developer", devUsernameField.getText(), new String(devPasswordField.getPassword()));
            }
        });

        logoutButton.addActionListener(e -> 
          mainFrame.showScreen("LoginPage"));
    }

    private void updateCredentials(String role, String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username or Password cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = db.updateCredentials(role, username, password);
        if (success) {
            JOptionPane.showMessageDialog(this, role + " credentials updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update " + role + " credentials.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
}
