package BookStoreManagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class SalesRecordPanel extends JPanel implements LanguageChangeListener {

    private Database db;
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private JButton createSaleButton, reverseSaleButton, refreshButton;
    private JScrollPane scrollPane;
    
    // Local variable to store current language (default: English)
    private String currentLanguage = "English";
    
    public SalesRecordPanel(Database db) {
        this.db = db;
        setLayout(new BorderLayout());

        // Initialize table model with default (English) column headers
        tableModel = new DefaultTableModel(new String[]{
            "Sale ID", "Customer Name", "Barcode", "Item Name", "Quantity", "Total Price", "Sale Date", "Reversed"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No editing allowed
            }
        };
        salesTable = new JTable(tableModel);
        // Increase table header font (e.g. to 18pt) based on language
        salesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));
        scrollPane = new JScrollPane(salesTable);
        add(scrollPane, BorderLayout.CENTER);

        // Top panel for buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // Create Sale Button
        createSaleButton = new JButton("Create Sale");
        createSaleButton.addActionListener(e -> openOrderDialog());
        topPanel.add(createSaleButton);

        // Reverse Sale Button
        reverseSaleButton = new JButton("Reverse Sale");
        reverseSaleButton.addActionListener(e -> reverseSale());
        topPanel.add(reverseSaleButton);

        // Refresh Button
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadSalesData());
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        // Load initial data
        loadSalesData();
    }
    
    private void openOrderDialog() {
        OrderDialog orderDialog = new OrderDialog((JFrame) SwingUtilities.getWindowAncestor(this), db);
        orderDialog.setVisible(true);
        loadSalesData(); // Refresh sales table after adding a sale
    }
    
    public void loadSalesData() {
        tableModel.setRowCount(0);
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT sale_id, barcode, customer_name, item_name, quantity, total_price, sale_date, reversed FROM sales");
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int saleId = rs.getInt("sale_id");
                String customerName = rs.getString("customer_name");
                String itemName = rs.getString("item_name");
                double totalAmount = rs.getDouble("total_price");
                int barcode = rs.getInt("barcode");
                int quantity = rs.getInt("quantity");
                String reversed = rs.getBoolean("reversed") ? (currentLanguage.equalsIgnoreCase("Urdu") ? "ہاں" : "Yes")
                        : (currentLanguage.equalsIgnoreCase("Urdu") ? "نہیں" : "No");
                Timestamp saleDate = rs.getTimestamp("sale_date");

                tableModel.addRow(new Object[]{saleId, customerName, barcode, itemName, quantity, totalAmount, saleDate, reversed});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void reverseSale() {
        int selectedRow = salesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                currentLanguage.equalsIgnoreCase("Urdu") ? "براہ کرم ریورس کرنے کے لیے سیلز منتخب کریں۔" : "Select a sale to reverse/unreverse.", 
                currentLanguage.equalsIgnoreCase("Urdu") ? "خرابی" : "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int saleId = (int) tableModel.getValueAt(selectedRow, 0);
        int quantity = (int) tableModel.getValueAt(selectedRow, 4);
        double totalPrice = (double) tableModel.getValueAt(selectedRow, 5);
        String reversedStatus = (String) tableModel.getValueAt(selectedRow, 7);

        if (reversedStatus.equals(currentLanguage.equalsIgnoreCase("Urdu") ? "ہاں" : "Yes")) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                currentLanguage.equalsIgnoreCase("Urdu") ?
                "یہ سیلز پہلے ہی ریورس ہو چکی ہے۔ کیا آپ اس کو ان ریورس کرنا چاہتے ہیں؟" :
                "This sale is already reversed. Do you want to unreversed it (undo the reversal)?",
                currentLanguage.equalsIgnoreCase("Urdu") ? "تصدیق کریں" : "Confirm Unreverse", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = db.getConnection()) {
                    conn.setAutoCommit(false);
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE sales SET reversed = 0 WHERE sale_id = ?")) {
                        stmt.setInt(1, saleId);
                        stmt.executeUpdate();
                    }
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE deals SET reversed = 0 WHERE sale_id = ?")) {
                        stmt.setInt(1, saleId);
                        stmt.executeUpdate();
                    }
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE inventory SET stock = stock - ?, sold = sold + ? WHERE item_id = (SELECT item_id FROM sales WHERE sale_id = ?)")) {
                        stmt.setInt(1, quantity);
                        stmt.setInt(2, quantity);
                        stmt.setInt(3, saleId);
                        stmt.executeUpdate();
                    }
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE customers SET balance = balance + ? WHERE customer_name = (SELECT customer_name FROM sales WHERE sale_id = ?)")) {
                        stmt.setDouble(1, totalPrice);
                        stmt.setInt(2, saleId);
                        stmt.executeUpdate();
                    }
                    conn.commit();
                    JOptionPane.showMessageDialog(this, 
                        currentLanguage.equalsIgnoreCase("Urdu") ? "سیلز کامیابی سے ان ریورس ہو گئی!" : "Sale unreversed successfully!", 
                        currentLanguage.equalsIgnoreCase("Urdu") ? "کامیابی" : "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadSalesData();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, 
                        currentLanguage.equalsIgnoreCase("Urdu") ? "سیلز ان ریورس کرنے میں خرابی!" : "Error unreversing sale!", 
                        currentLanguage.equalsIgnoreCase("Urdu") ? "ڈیٹا بیس خرابی" : "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            int confirm = JOptionPane.showConfirmDialog(this, 
                currentLanguage.equalsIgnoreCase("Urdu") ?
                "کیا آپ واقعی اس سیلز کو ریورس کرنا چاہتے ہیں؟" :
                "Are you sure you want to reverse this sale?",
                currentLanguage.equalsIgnoreCase("Urdu") ? "تصدیق کریں" : "Confirm Reverse", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = db.getConnection()) {
                    conn.setAutoCommit(false);
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE sales SET reversed = 1 WHERE sale_id = ?")) {
                        stmt.setInt(1, saleId);
                        stmt.executeUpdate();
                    }
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE deals SET reversed = 1 WHERE sale_id = ?")) {
                        stmt.setInt(1, saleId);
                        stmt.executeUpdate();
                    }
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE inventory SET stock = stock + ?, sold = sold - ? WHERE item_id = (SELECT item_id FROM sales WHERE sale_id = ?)")) {
                        stmt.setInt(1, quantity);
                        stmt.setInt(2, quantity);
                        stmt.setInt(3, saleId);
                        stmt.executeUpdate();
                    }
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE customers SET balance = balance - ? WHERE customer_name = (SELECT customer_name FROM sales WHERE sale_id = ?)")) {
                        stmt.setDouble(1, totalPrice);
                        stmt.setInt(2, saleId);
                        stmt.executeUpdate();
                    }
                    conn.commit();
                    JOptionPane.showMessageDialog(this, 
                        currentLanguage.equalsIgnoreCase("Urdu") ? "سیلز کامیابی سے ریورس ہو گئی!" : "Sale reversed successfully!", 
                        currentLanguage.equalsIgnoreCase("Urdu") ? "کامیابی" : "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadSalesData();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, 
                        currentLanguage.equalsIgnoreCase("Urdu") ? "سیلز ریورس کرنے میں خرابی!" : "Error reversing sale!", 
                        currentLanguage.equalsIgnoreCase("Urdu") ? "ڈیٹا بیس خرابی" : "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    // ----------------- Language Change Implementation -----------------
    
   
    public void updateLanguage(String language) {
        currentLanguage = language;
        applyLanguage();
    }
    
    @Override
    public void applyLanguage() {
        // Update button texts and fonts without recreating the buttons
        if (currentLanguage.equalsIgnoreCase("Urdu")) {
            createSaleButton.setText("سیلز بنائیں");
            reverseSaleButton.setText("سیلز ریورس کریں");
            refreshButton.setText("ریفریش");
            tableModel.setColumnIdentifiers(new String[]{
                "سیلز آئی ڈی", "گاہک کا نام", "بارکوڈ", "آئٹم کا نام", "مقدار", "کل قیمت", "سیلز کی تاریخ", "ریورس شدہ"
            });
            Font urduFont = new Font("Jameel Noori Nastaleeq", Font.BOLD, 18);
            createSaleButton.setFont(urduFont);
            reverseSaleButton.setFont(urduFont);
            refreshButton.setFont(urduFont);
            // Update table header font
            salesTable.getTableHeader().setFont(urduFont.deriveFont(urduFont.getSize2D() * 4/3f));
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        } else {
            createSaleButton.setText("Create Sale");
            reverseSaleButton.setText("Reverse Sale");
            refreshButton.setText("Refresh");
            tableModel.setColumnIdentifiers(new String[]{
                "Sale ID", "Customer Name", "Barcode", "Item Name", "Quantity", "Total Price", "Sale Date", "Reversed"
            });
            Font englishFont = new Font("Arial", Font.BOLD, 18);
            createSaleButton.setFont(englishFont);
            reverseSaleButton.setFont(englishFont);
            refreshButton.setFont(englishFont);
            salesTable.getTableHeader().setFont(englishFont.deriveFont(englishFont.getSize2D() * 4/3f));
            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        }
        revalidate();
        repaint();
    }
}
