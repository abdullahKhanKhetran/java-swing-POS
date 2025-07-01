package BookStoreManagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class TransactionsPanel extends JPanel {
    private Database db;
    private JTable transactionsTable;
    private DefaultTableModel tableModel;
    private JTextField filterField;
    private JComboBox<String> sortCombo;
    private JButton filterButton, refreshButton;
    
    // Labels for dynamic bilingual support
    private JLabel filterLabel, sortByLabel;

    public TransactionsPanel(JFrame parent, Database db) {
        this.db = db;
        setLayout(new BorderLayout());
        
        // Create filter and sort panel at the top
        JPanel filterSortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Create labels as instance variables
        filterLabel = new JLabel("Filter:");
        filterSortPanel.add(filterLabel);
        
        filterField = new JTextField(15);
        filterSortPanel.add(filterField);
        
        sortByLabel = new JLabel("Sort By:");
        filterSortPanel.add(sortByLabel);
        
        // Create sort combo with default English items
        sortCombo = new JComboBox<>(new String[]{"Date", "Amount", "Transaction ID"});
        filterSortPanel.add(sortCombo);
        
        filterButton = new JButton("Apply");
        filterSortPanel.add(filterButton);
        
        refreshButton = new JButton("Refresh");
        filterSortPanel.add(refreshButton);
        
        add(filterSortPanel, BorderLayout.NORTH);
        
        // Create table model without static columns
        tableModel = new DefaultTableModel();
        transactionsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Load transactions initially (no filter)
        loadTransactions("");
        
        // Filter button applies the filter from the text field
        filterButton.addActionListener(e -> {
            String filterText = filterField.getText().trim();
            loadTransactions(filterText);
        });
        
        // Refresh button resets the filter and reloads the data
        refreshButton.addActionListener(e -> {
            filterField.setText("");
            loadTransactions("");
        });
    }
    
    // Method to update UI texts based on language
    public void updateLanguage(String lang) {
        if (lang.equalsIgnoreCase("Urdu")) {
            filterLabel.setText("فلٹر:");
            sortByLabel.setText("ترتیب حسب:");
            filterButton.setText("درخواست دیں");
            refreshButton.setText("تازہ کریں");
            // Update sortCombo items in Urdu
            sortCombo.removeAllItems();
            sortCombo.addItem("تاریخ");  // Date
            sortCombo.addItem("رقم");     // Amount
            sortCombo.addItem("ٹرانزیکشن آئی ڈی"); // Transaction ID
        } else {
            filterLabel.setText("Filter:");
            sortByLabel.setText("Sort By:");
            filterButton.setText("Apply");
            refreshButton.setText("Refresh");
            // Update sortCombo items in English
            sortCombo.removeAllItems();
            sortCombo.addItem("Date");
            sortCombo.addItem("Amount");
            sortCombo.addItem("Transaction ID");
        }
        revalidate();
        repaint();
    }
    
    // Loads transactions from the database dynamically based on the ResultSet metadata.
    private void loadTransactions(String filter) {
        tableModel.setRowCount(0); // Clear existing rows
        String query = "SELECT * FROM transactions";
        if (!filter.isEmpty()) {
            query += " WHERE transaction_id LIKE '%" + filter + "%'";
        }
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
             
            // Get metadata to build column names dynamically
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            Vector<String> columnNames = new Vector<>();
            for (int i = 1; i <= colCount; i++) {
                columnNames.add(meta.getColumnName(i));
            }
            tableModel.setColumnIdentifiers(columnNames);
            
            // Populate rows dynamically
            while (rs.next()) {
                Vector<Object> rowData = new Vector<>();
                for (int i = 1; i <= colCount; i++) {
                    rowData.add(rs.getObject(i));
                }
                tableModel.addRow(rowData);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading transactions!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
