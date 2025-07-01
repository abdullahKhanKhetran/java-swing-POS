package BookStoreManagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class ManageCustomersPanel extends JPanel {
    private Database db;
    private JTable customersTable;
    private DefaultTableModel tableModel;
    private JButton addButton, saveButton, deleteButton, refreshButton, dealsButton, settleUpButton, transactionDetailsButton, exchangeReceiverButton;
    private JScrollPane scrollPane;
    private JTextField searchField;
    private JComboBox<String> searchByComboBox, sortByComboBox;
    private JRadioButton ascRadioButton, descRadioButton;

    public ManageCustomersPanel(Database db) {
        this.db = db;
        setLayout(new BorderLayout());

        // Setup table model with fixed columns
        String[] columnNames = {"customer_id", "customer_name", "phone", "cnic", "address", "Remaining", "Advance"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Allow editing for all columns except customer_id and computed columns (Remaining and Advance)
                return column != 0 && column != 5 && column != 6;
            }
        };
        customersTable = new JTable(tableModel);
        scrollPane = new JScrollPane(customersTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        // Top panel with search and sorting options, plus buttons
        JPanel topPanel = new JPanel(new BorderLayout());

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchByComboBox = new JComboBox<>(new String[]{"Name", "Phone", "CNIC", "Address"});
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchCustomers());

        // Sort panel: choose sort column and order
        sortByComboBox = new JComboBox<>(new String[]{"customer_id", "customer_name", "phone", "cnic", "address", "balance"});
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
        addButton = new JButton("Add Customer");
        saveButton = new JButton("Save");
        deleteButton = new JButton("Delete Customer");
        refreshButton = new JButton("Refresh");
        dealsButton = new JButton("Deals and Details");
        settleUpButton = new JButton("Settle Up");
        transactionDetailsButton = new JButton("Transaction Details");
        exchangeReceiverButton = new JButton("Exchange Receiver");

        addButton.addActionListener(e -> addCustomer());
        saveButton.addActionListener(e -> saveCustomer());
        deleteButton.addActionListener(e -> deleteCustomer());
        refreshButton.addActionListener(e -> loadCustomersData());
        dealsButton.addActionListener(e -> openDealsDialog());
        settleUpButton.addActionListener(e -> settleUpCustomer());
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

        loadCustomersData();
    }

    private void loadCustomersData() {
        tableModel.setRowCount(0);
        // Query customers and their balance from the DB.
        String query = "SELECT customer_id, customer_name, phone, cnic, address, balance FROM customers";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int customerId = rs.getInt("customer_id");
                String name = rs.getString("customer_name");
                String phone = rs.getString("phone");
                String cnic = rs.getString("cnic");
                String address = rs.getString("address");
                double balance = rs.getDouble("balance");
                double remaining = balance > 0 ? balance : 0;
                double advance = balance < 0 ? -balance : 0;
                Vector<Object> rowData = new Vector<>();
                rowData.add(customerId);
                rowData.add(name);
                rowData.add(phone);
                rowData.add(cnic);
                rowData.add(address);
                rowData.add(remaining);
                rowData.add(advance);
                tableModel.addRow(rowData);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading customers data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchCustomers() {
        String searchText = searchField.getText().trim();
        String searchBy = (String) searchByComboBox.getSelectedItem();
        String sortBy = (String) sortByComboBox.getSelectedItem();
        String sortOrder = ascRadioButton.isSelected() ? "ASC" : "DESC";

        if (searchText.isEmpty()) {
            loadCustomersData();
            return;
        }

        String columnName;
        switch (searchBy) {
            case "Name":
                columnName = "customer_name";
                break;
            case "Phone":
                columnName = "phone";
                break;
            case "CNIC":
                columnName = "cnic";
                break;
            case "Address":
                columnName = "address";
                break;
            default:
                columnName = "customer_name";
        }

        String query = "SELECT customer_id, customer_name, phone, cnic, address, balance FROM customers WHERE " + columnName + " LIKE ? ORDER BY " + sortBy + " " + sortOrder;
        tableModel.setRowCount(0);
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + searchText + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int customerId = rs.getInt("customer_id");
                String name = rs.getString("customer_name");
                String phone = rs.getString("phone");
                String cnic = rs.getString("cnic");
                String address = rs.getString("address");
                double balance = rs.getDouble("balance");
                double remaining = balance > 0 ? balance : 0;
                double advance = balance < 0 ? -balance : 0;
                Vector<Object> rowData = new Vector<>();
                rowData.add(customerId);
                rowData.add(name);
                rowData.add(phone);
                rowData.add(cnic);
                rowData.add(address);
                rowData.add(remaining);
                rowData.add(advance);
                tableModel.addRow(rowData);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching customers: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCustomer() {
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField cnicField = new JTextField();
        JTextField addressField = new JTextField();

        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Phone:"));
        inputPanel.add(phoneField);
        inputPanel.add(new JLabel("CNIC:"));
        inputPanel.add(cnicField);
        inputPanel.add(new JLabel("Address:"));
        inputPanel.add(addressField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel, "Add Customer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String cnic = cnicField.getText().trim();
            String address = addressField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO customers (customer_name, phone, cnic, address, balance) VALUES (?, ?, ?, ?, 0)")) {
                stmt.setString(1, name);
                stmt.setString(2, phone);
                stmt.setString(3, cnic);
                stmt.setString(4, address);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Customer added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCustomersData();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding customer!", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveCustomer() {
        int selectedRow = customersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a customer to save changes.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        String phone = (String) tableModel.getValueAt(selectedRow, 2);
        String cnic = (String) tableModel.getValueAt(selectedRow, 3);
        String address = (String) tableModel.getValueAt(selectedRow, 4);

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE customers SET customer_name = ?, phone = ?, cnic = ?, address = ? WHERE customer_id = ?")) {
            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.setString(3, cnic);
            stmt.setString(4, address);
            stmt.setInt(5, customerId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Customer updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCustomersData();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating customer!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCustomer() {
        int selectedRow = customersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a customer to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this customer?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM customers WHERE customer_id = ?")) {
                stmt.setInt(1, customerId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Customer deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCustomersData();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting customer!", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openDealsDialog() {
        int selectedRow = customersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a customer to view deals.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        String cnic = (String) tableModel.getValueAt(selectedRow, 3);
        String phone = (String) tableModel.getValueAt(selectedRow, 2);

        String title = name + " (" + cnic + ") (" + phone + ")";
        DealsDialog dealsDialog = new DealsDialog((JFrame) SwingUtilities.getWindowAncestor(this), title, customerId, "customer", db);
        dealsDialog.setVisible(true);
    }

private void settleUpCustomer() {
    int selectedRow = customersTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Select a customer to settle up.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    int customerId = (int) tableModel.getValueAt(selectedRow, 0);
    String customerName = (String) tableModel.getValueAt(selectedRow, 1);
    double remaining = Double.parseDouble(tableModel.getValueAt(selectedRow, 5).toString());
    double advance = Double.parseDouble(tableModel.getValueAt(selectedRow, 6).toString());
    double balanceValue = remaining > 0 ? remaining : -advance;

    JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
    JTextField amountField = new JTextField();
    
    // Radio buttons for transaction type
    JRadioButton customerPaidYou = new JRadioButton("Customer paid you");
    JRadioButton youPaidCustomer = new JRadioButton("You paid customer");
    ButtonGroup group = new ButtonGroup();
    group.add(customerPaidYou);
    group.add(youPaidCustomer);
    
    // Default selection based on balance value
    if (balanceValue >= 0) {
        customerPaidYou.setSelected(true);
    } else {
        youPaidCustomer.setSelected(true);
    }
    
    panel.add(new JLabel("Transaction Type:"));
    JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    typePanel.add(customerPaidYou);
    typePanel.add(youPaidCustomer);
    panel.add(typePanel);
    panel.add(new JLabel("Amount:"));
    panel.add(amountField);

    int result = JOptionPane.showConfirmDialog(this, panel, "Settle Up for " + customerName, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
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
            if (customerPaidYou.isSelected()) {
                // Customer paid you, so decrease the customer's balance
                try (PreparedStatement stmt = conn.prepareStatement("UPDATE customers SET balance = balance - ? WHERE customer_id = ?")) {
                    stmt.setDouble(1, amount);
                    stmt.setInt(2, customerId);
                    stmt.executeUpdate();
                }
                transactionType = "payment_received";
            } else if (youPaidCustomer.isSelected()) {
                // You paid the customer, so increase the customer's balance
                try (PreparedStatement stmt = conn.prepareStatement("UPDATE customers SET balance = balance + ? WHERE customer_id = ?")) {
                    stmt.setDouble(1, amount);
                    stmt.setInt(2, customerId);
                    stmt.executeUpdate();
                }
                transactionType = "payment_done";
            }
            // Insert the transaction record for the customer
            try (PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO transactions (customer_id, amount, type) VALUES (?, ?, ?)")) {
                stmt2.setInt(1, customerId);
                stmt2.setDouble(2, amount);
                stmt2.setString(3, transactionType);
                stmt2.executeUpdate();
            }
            conn.commit();
        }
        JOptionPane.showMessageDialog(this, "Payment recorded and balance updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        loadCustomersData();
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Invalid amount. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error updating balance: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}


    private void showTransactionDetails() {
        int selectedRow = customersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a customer to view transactions.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        String customerName = (String) tableModel.getValueAt(selectedRow, 1);
        String phone = (String) tableModel.getValueAt(selectedRow, 2);
        String address = (String) tableModel.getValueAt(selectedRow, 4);

        String title = customerName + " (" + phone + ") (" + address + ")";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT transaction_id, type, amount, date FROM transactions WHERE customer_id = ?")) {
            stmt.setInt(1, customerId);
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

    // New button action for "Exchange Receiver"
    private void exchangeReceiver() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField sourceIdField = new JTextField();
        JTextField receiverIdField = new JTextField();
        JTextField amountField = new JTextField();
        panel.add(new JLabel("Source Customer ID:"));
        panel.add(sourceIdField);
        panel.add(new JLabel("Receiver Customer ID:"));
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
                    try (PreparedStatement stmt = conn.prepareStatement("SELECT balance FROM customers WHERE customer_id = ?")) {
                        stmt.setInt(1, sourceId);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            sourceBalance = rs.getDouble("balance");
                        } else {
                            JOptionPane.showMessageDialog(this, "Source customer not found.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    // For transferring funds, if source owes (balance > 0) then amount should not exceed balance.
                    // If source has an advance (balance < 0), amount should not exceed its absolute value.
                    if (sourceBalance >= 0) {
                        if (amount > sourceBalance) {
                            JOptionPane.showMessageDialog(this, "Source customer does not owe that much.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } else {
                        if (amount > -sourceBalance) {
                            JOptionPane.showMessageDialog(this, "Source customer's advance is less than the amount.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE customers SET balance = balance - ? WHERE customer_id = ?")) {
                        stmt.setDouble(1, amount);
                        stmt.setInt(2, sourceId);
                        stmt.executeUpdate();
                    }
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE customers SET balance = balance + ? WHERE customer_id = ?")) {
                        stmt.setDouble(1, amount);
                        stmt.setInt(2, receiverId);
                        stmt.executeUpdate();
                    }
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Exchange successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCustomersData();
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
