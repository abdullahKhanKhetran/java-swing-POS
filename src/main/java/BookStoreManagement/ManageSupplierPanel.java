package BookStoreManagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class ManageSupplierPanel extends JPanel {
    private Database db;
    private JTable suppliersTable;
    private DefaultTableModel tableModel;
    private JButton addButton, saveButton, deleteButton, refreshButton, dealsButton, settleUpButton, transactionDetailsButton, exchangeReceiverButton;
    private JScrollPane scrollPane;
    private JTextField searchField;
    private JComboBox<String> searchByComboBox, sortByComboBox;
    private JRadioButton ascRadioButton, descRadioButton;

    public ManageSupplierPanel(Database db) {
        this.db = db;
        setLayout(new BorderLayout());

        // Setup table model with fixed columns: supplier_id, supplier_name, phone, email, address, Remaining, Advance
        String[] columnNames = {"supplier_id", "supplier_name", "phone", "email", "address", "Remaining", "Advance"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Allow editing except supplier_id and computed columns (Remaining and Advance)
                return column != 0 && column != 5 && column != 6;
            }
        };
        suppliersTable = new JTable(tableModel);
        scrollPane = new JScrollPane(suppliersTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        // Top panel: search/sort options and buttons
        JPanel topPanel = new JPanel(new BorderLayout());

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchByComboBox = new JComboBox<>(new String[]{"Name", "Phone", "Email", "Address"});
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchSuppliers());

        // Sort panel
        sortByComboBox = new JComboBox<>(new String[]{"supplier_id", "supplier_name", "phone", "email", "address", "balance"});
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ascRadioButton = new JRadioButton("ASC", true);
        descRadioButton = new JRadioButton("DESC");
        ButtonGroup sortGroup = new ButtonGroup();
        sortGroup.add(ascRadioButton);
        sortGroup.add(descRadioButton);
        sortPanel.add(new JLabel("Sort By:"));
        sortPanel.add(sortByComboBox);
        sortPanel.add(new JLabel("Sort Order:"));
        sortPanel.add(ascRadioButton);
        sortPanel.add(descRadioButton);

        searchPanel.add(new JLabel("Search By:"));
        searchPanel.add(searchByComboBox);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(sortPanel);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addButton = new JButton("Add Supplier");
        saveButton = new JButton("Save");
        deleteButton = new JButton("Delete Supplier");
        refreshButton = new JButton("Refresh");
        dealsButton = new JButton("Deals and Details");
        settleUpButton = new JButton("Settle Up");
        transactionDetailsButton = new JButton("Transaction Details");
        exchangeReceiverButton = new JButton("Exchange Receiver");

        addButton.addActionListener(e -> addSupplier());
        saveButton.addActionListener(e -> saveSupplier());
        deleteButton.addActionListener(e -> deleteSupplier());
        refreshButton.addActionListener(e -> loadSuppliersData());
        dealsButton.addActionListener(e -> openDealsDialog());
        settleUpButton.addActionListener(e -> settleUpSupplier());
        transactionDetailsButton.addActionListener(e -> showTransactionDetails());
        exchangeReceiverButton.addActionListener(e -> exchangeReceiver());

        buttonPanel.add(addButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(dealsButton);
        buttonPanel.add(settleUpButton);
        buttonPanel.add(transactionDetailsButton);
        buttonPanel.add(exchangeReceiverButton);

        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        loadSuppliersData();
    }

    private void loadSuppliersData() {
        tableModel.setRowCount(0);
        // Fixed query to load supplier data; assume suppliers table has columns: supplier_id, supplier_name, phone, email, address, balance
        String query = "SELECT supplier_id, supplier_name, phone, email, address, balance FROM suppliers";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int supplierId = rs.getInt("supplier_id");
                String name = rs.getString("supplier_name");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                String address = rs.getString("address");
                double balance = rs.getDouble("balance");
                double remaining = balance > 0 ? balance : 0;
                double advance = balance < 0 ? -balance : 0;
                Vector<Object> rowData = new Vector<>();
                rowData.add(supplierId);
                rowData.add(name);
                rowData.add(phone);
                rowData.add(email);
                rowData.add(address);
                rowData.add(remaining);
                rowData.add(advance);
                tableModel.addRow(rowData);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading suppliers data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchSuppliers() {
        String searchText = searchField.getText().trim();
        String searchBy = (String) searchByComboBox.getSelectedItem();
        String sortBy = (String) sortByComboBox.getSelectedItem();
        String sortOrder = ascRadioButton.isSelected() ? "ASC" : "DESC";

        if (searchText.isEmpty()) {
            loadSuppliersData();
            return;
        }

        String columnName;
        switch (searchBy) {
            case "Name":
                columnName = "supplier_name";
                break;
            case "Phone":
                columnName = "phone";
                break;
            case "Email":
                columnName = "email";
                break;
            case "Address":
                columnName = "address";
                break;
            default:
                columnName = "supplier_name";
        }

        String query = "SELECT supplier_id, supplier_name, phone, email, address, balance FROM suppliers WHERE " 
                + columnName + " LIKE ? ORDER BY " + sortBy + " " + sortOrder;
        tableModel.setRowCount(0);
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + searchText + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int supplierId = rs.getInt("supplier_id");
                String name = rs.getString("supplier_name");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                String address = rs.getString("address");
                double balance = rs.getDouble("balance");
                double remaining = balance > 0 ? balance : 0;
                double advance = balance < 0 ? -balance : 0;
                Vector<Object> rowData = new Vector<>();
                rowData.add(supplierId);
                rowData.add(name);
                rowData.add(phone);
                rowData.add(email);
                rowData.add(address);
                rowData.add(remaining);
                rowData.add(advance);
                tableModel.addRow(rowData);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching suppliers: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addSupplier() {
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField addressField = new JTextField();

        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Phone:"));
        inputPanel.add(phoneField);
        inputPanel.add(new JLabel("Email:"));
        inputPanel.add(emailField);
        inputPanel.add(new JLabel("Address:"));
        inputPanel.add(addressField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel, "Add Supplier", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO suppliers (supplier_name, phone, email, address, balance) VALUES (?, ?, ?, ?, 0)")) {
                stmt.setString(1, name);
                stmt.setString(2, phone);
                stmt.setString(3, email);
                stmt.setString(4, address);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Supplier added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSuppliersData();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding supplier!", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveSupplier() {
        int selectedRow = suppliersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a supplier to save changes.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int supplierId = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        String phone = (String) tableModel.getValueAt(selectedRow, 2);
        String email = (String) tableModel.getValueAt(selectedRow, 3);
        String address = (String) tableModel.getValueAt(selectedRow, 4);

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE suppliers SET supplier_name = ?, phone = ?, email = ?, address = ? WHERE supplier_id = ?")) {
            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.setString(3, email);
            stmt.setString(4, address);
            stmt.setInt(5, supplierId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Supplier updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadSuppliersData();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating supplier!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSupplier() {
        int selectedRow = suppliersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a supplier to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int supplierId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this supplier?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM suppliers WHERE supplier_id = ?")) {
                stmt.setInt(1, supplierId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Supplier deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSuppliersData();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting supplier!", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openDealsDialog() {
        int selectedRow = suppliersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a supplier to view deals.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int supplierId = (int) tableModel.getValueAt(selectedRow, 0);
        new SupplierDealsDialog((JFrame) SwingUtilities.getWindowAncestor(this), supplierId, db).setVisible(true);
    }

    private void settleUpSupplier() {
    int selectedRow = suppliersTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Select a supplier to settle up.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    int supplierId = (int) tableModel.getValueAt(selectedRow, 0);
    String supplierName = (String) tableModel.getValueAt(selectedRow, 1);
    // For suppliers, assume "Remaining" (index 5) is what we owe them and "Advance" (index 6) is what they owe us.
    double remaining = Double.parseDouble(tableModel.getValueAt(selectedRow, 5).toString());
    double advance = Double.parseDouble(tableModel.getValueAt(selectedRow, 6).toString());
    double balanceValue = remaining > 0 ? remaining : -advance;

    // Create a dialog with toggle options
    JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
    JTextField amountField = new JTextField();
    
    JRadioButton youPaidSupplier = new JRadioButton("You paid supplier");
    JRadioButton supplierPaidYou = new JRadioButton("Supplier paid you");
    ButtonGroup group = new ButtonGroup();
    group.add(youPaidSupplier);
    group.add(supplierPaidYou);
    
    // Default selection based on balance value
    if (balanceValue >= 0) {
        youPaidSupplier.setSelected(true);
    } else {
        supplierPaidYou.setSelected(true);
    }
    
    panel.add(new JLabel("Transaction Type:"));
    JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    typePanel.add(youPaidSupplier);
    typePanel.add(supplierPaidYou);
    panel.add(typePanel);
    panel.add(new JLabel("Amount:"));
    panel.add(amountField);
    
    int result = JOptionPane.showConfirmDialog(this, panel, "Settle Up for " + supplierName, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION) {
        return;
    }
    
    try {
        double amount = Double.parseDouble(amountField.getText().trim());
        if (amount <= 0) {
            JOptionPane.showMessageDialog(this, "Amount must be greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String transactionType = "";
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            if (youPaidSupplier.isSelected()) {
                // We pay the supplier, so decrease our liability
                try (PreparedStatement stmt = conn.prepareStatement("UPDATE suppliers SET balance = balance - ? WHERE supplier_id = ?")) {
                    stmt.setDouble(1, amount);
                    stmt.setInt(2, supplierId);
                    stmt.executeUpdate();
                }
                transactionType = "payment_done";
            } else if (supplierPaidYou.isSelected()) {
                // Supplier pays you, so increase our balance (reduce negative advance)
                try (PreparedStatement stmt = conn.prepareStatement("UPDATE suppliers SET balance = balance + ? WHERE supplier_id = ?")) {
                    stmt.setDouble(1, amount);
                    stmt.setInt(2, supplierId);
                    stmt.executeUpdate();
                }
                transactionType = "payment_received";
            }
            // Insert the transaction record
            try (PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO transactions (supplier_id, amount, type) VALUES (?, ?, ?)")) {
                stmt2.setInt(1, supplierId);
                stmt2.setDouble(2, amount);
                stmt2.setString(3, transactionType);
                stmt2.executeUpdate();
            }
            conn.commit();
        }
        JOptionPane.showMessageDialog(this, "Payment recorded and balance updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        loadSuppliersData();
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Invalid amount. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error updating balance: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void showTransactionDetails() {
        int selectedRow = suppliersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a supplier to view transactions.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int supplierId = (int) tableModel.getValueAt(selectedRow, 0);
        String supplierName = (String) tableModel.getValueAt(selectedRow, 1);
        String phone = (String) tableModel.getValueAt(selectedRow, 2);
        String address = (String) tableModel.getValueAt(selectedRow, 4);

        String title = supplierName + " (" + phone + ") (" + address + ")";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT transaction_id, type, amount, date FROM transactions WHERE supplier_id = ?")) {
            stmt.setInt(1, supplierId);
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel transactionModel = new DefaultTableModel();
            transactionModel.addColumn("Transaction ID");
            transactionModel.addColumn("Type");
            transactionModel.addColumn("Amount");
            transactionModel.addColumn("Date");

            while (rs.next()) {
                transactionModel.addRow(new Object[]{
                        rs.getInt("transaction_id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getTimestamp("date")
                });
            }
            JTable transactionTable = new JTable(transactionModel);
            JScrollPane scrollPane = new JScrollPane(transactionTable);
            JOptionPane.showMessageDialog(this, scrollPane, "Transaction Details for " + title, JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching transactions: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Optional: if you want an exchange receiver feature for suppliers too.
    private void exchangeReceiver() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField sourceIdField = new JTextField();
        JTextField receiverIdField = new JTextField();
        JTextField amountField = new JTextField();
        panel.add(new JLabel("Source Supplier ID:"));
        panel.add(sourceIdField);
        panel.add(new JLabel("Receiver Supplier ID:"));
        panel.add(receiverIdField);
        panel.add(new JLabel("Amount to Transfer:"));
        panel.add(amountField);
        int result = JOptionPane.showConfirmDialog(this, panel, "Exchange Receiver", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int sourceId = Integer.parseInt(sourceIdField.getText().trim());
                int receiverId = Integer.parseInt(receiverIdField.getText().trim());
                double amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Amount must be greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try (Connection conn = db.getConnection()) {
                    conn.setAutoCommit(false);
                    double sourceBalance = 0;
                    try (PreparedStatement stmt = conn.prepareStatement("SELECT balance FROM suppliers WHERE supplier_id = ?")) {
                        stmt.setInt(1, sourceId);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            sourceBalance = rs.getDouble("balance");
                        } else {
                            JOptionPane.showMessageDialog(this, "Source supplier not found.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    if (sourceBalance >= 0) {
                        if (amount > sourceBalance) {
                            JOptionPane.showMessageDialog(this, "Source supplier does not owe that much.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } else {
                        if (amount > -sourceBalance) {
                            JOptionPane.showMessageDialog(this, "Source supplier's advance is less than the amount.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE suppliers SET balance = balance - ? WHERE supplier_id = ?")) {
                        stmt.setDouble(1, amount);
                        stmt.setInt(2, sourceId);
                        stmt.executeUpdate();
                    }
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE suppliers SET balance = balance + ? WHERE supplier_id = ?")) {
                        stmt.setDouble(1, amount);
                        stmt.setInt(2, receiverId);
                        stmt.executeUpdate();
                    }
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Exchange successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadSuppliersData();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error during exchange: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
