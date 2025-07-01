package BookStoreManagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class PurchaseRecordPanel extends JPanel implements LanguageChangeListener {

    private Database db;
    private JTable purchasesTable;
    private DefaultTableModel tableModel;
    private JButton createPurchaseButton, reversePurchaseButton, refreshButton;
    private JScrollPane scrollPane;
    private JPanel topPanel;
    
    // Local variable to store current language (default: English)
    private String currentLanguage = "English";

    public PurchaseRecordPanel(Database db) {
        this.db = db;
        setLayout(new BorderLayout());
        
        // Initialize table model with default (English) column headers
        tableModel = new DefaultTableModel(new String[]{
            "Purchase ID", "Item Name", "Barcode", "Supplier ID", "Quantity", "Total Price", "Purchase Date", "Payment Type", "Reversed"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        purchasesTable = new JTable(tableModel);
        // Set table header font
        purchasesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));
        scrollPane = new JScrollPane(purchasesTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Top panel for buttons
        topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        createPurchaseButton = new JButton("Create Purchase");
        createPurchaseButton.addActionListener(e -> openPurchaseOrderDialog());
        topPanel.add(createPurchaseButton);
        
        reversePurchaseButton = new JButton("Reverse Purchase");
        reversePurchaseButton.addActionListener(e -> reversePurchase());
        topPanel.add(reversePurchaseButton);
        
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadPurchasesData());
        topPanel.add(refreshButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Load initial data
        loadPurchasesData();
    }
    
    private void openPurchaseOrderDialog() {
        PurchaseOrderDialog purchaseOrderDialog = new PurchaseOrderDialog((JFrame) SwingUtilities.getWindowAncestor(this), db);
        purchaseOrderDialog.setVisible(true);
        loadPurchasesData(); // Refresh data after adding a purchase
    }
    
    public void loadPurchasesData() {
        tableModel.setRowCount(0);
        // Using only the purchases table
        String query = "SELECT purchase_id, item_name, barcode, supplier_id, quantity, total_price, purchase_date, payment_type, reversed FROM purchases WHERE purchase_id >= 0";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Vector<Object> rowData = new Vector<>();
                rowData.add(rs.getInt("purchase_id"));
                rowData.add(rs.getString("item_name"));
                rowData.add(rs.getString("barcode"));
                rowData.add(rs.getInt("supplier_id"));
                rowData.add(rs.getInt("quantity"));
                rowData.add(rs.getDouble("total_price"));
                rowData.add(rs.getDate("purchase_date"));
                rowData.add(rs.getString("payment_type"));
                rowData.add(rs.getInt("reversed") == 1 
                        ? (currentLanguage.equalsIgnoreCase("Urdu") ? "ہاں" : "Yes") 
                        : (currentLanguage.equalsIgnoreCase("Urdu") ? "نہیں" : "No"));
                tableModel.addRow(rowData);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                currentLanguage.equalsIgnoreCase("Urdu") ? "خریداری کا ڈیٹا لوڈ کرنے میں خرابی!" : "Error loading purchases data!", 
                currentLanguage.equalsIgnoreCase("Urdu") ? "ڈیٹا بیس خرابی" : "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void reversePurchase() {
        int selectedRow = purchasesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                currentLanguage.equalsIgnoreCase("Urdu") ? "براہ کرم الٹ کرنے کے لیے خریداری منتخب کریں۔" : "Select a purchase to reverse/unreverse.", 
                currentLanguage.equalsIgnoreCase("Urdu") ? "خرابی" : "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int purchaseId = (int) tableModel.getValueAt(selectedRow, 0);
        int quantity = (int) tableModel.getValueAt(selectedRow, 4);
        double totalPrice = (double) tableModel.getValueAt(selectedRow, 5);
        String reversedStatus = (String) tableModel.getValueAt(selectedRow, 8);
        
        // If already reversed, prompt to unreversed it
        if (reversedStatus.equals(currentLanguage.equalsIgnoreCase("Urdu") ? "ہاں" : "Yes")) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                currentLanguage.equalsIgnoreCase("Urdu") ? 
                "یہ خریداری پہلے ہی ریورس ہو چکی ہے۔ کیا آپ اسے ان ریورس کرنا چاہتے ہیں؟" : 
                "This purchase is already reversed. Do you want to unreversed it (undo the reversal)?", 
                currentLanguage.equalsIgnoreCase("Urdu") ? "تصدیق کریں" : "Confirm Unreverse", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = db.getConnection()) {
                    conn.setAutoCommit(false);
                    
                    // Mark purchase as not reversed
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE purchases SET reversed = 0 WHERE purchase_id = ?")) {
                        stmt.setInt(1, purchaseId);
                        stmt.executeUpdate();
                    }
                    // Mark deals as not reversed
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE deals SET reversed = 0 WHERE purchase_id = ?")) {
                        stmt.setInt(1, purchaseId);
                        stmt.executeUpdate();
                    }
                    // Revert inventory changes: add back deducted stock
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE inventory SET stock = stock + (SELECT quantity FROM purchases WHERE purchase_id = ?) " +
                            "WHERE item_id = (SELECT item_id FROM purchases WHERE purchase_id = ?)")) {
                        stmt.setInt(1, purchaseId);
                        stmt.setInt(2, purchaseId);
                        stmt.executeUpdate();
                    }
                    // Revert supplier credit if payment was on account
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE suppliers SET balance = balance + (SELECT total_price FROM purchases WHERE purchase_id = ?) " +
                            "WHERE supplier_id = (SELECT supplier_id FROM purchases WHERE purchase_id = ?) " +
                            "AND EXISTS (SELECT 1 FROM purchases WHERE purchase_id = ? AND payment_type = 'account')")) {
                        stmt.setInt(1, purchaseId);
                        stmt.setInt(2, purchaseId);
                        stmt.setInt(3, purchaseId);
                        stmt.executeUpdate();
                    }
                    conn.commit();
                    JOptionPane.showMessageDialog(this, 
                        currentLanguage.equalsIgnoreCase("Urdu") ? "خریداری کی الٹ کاری منسوخ کر دی گئی ہے!" : "Purchase unreversed successfully!", 
                        currentLanguage.equalsIgnoreCase("Urdu") ? "کامیابی" : "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadPurchasesData();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, 
                        currentLanguage.equalsIgnoreCase("Urdu") ? "خریداری ان ریورس کرنے میں خرابی!" : "Error unreversing purchase!", 
                        currentLanguage.equalsIgnoreCase("Urdu") ? "ڈیٹا بیس خرابی" : "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {  // Not yet reversed; confirm reversal
            int confirm = JOptionPane.showConfirmDialog(this, 
                currentLanguage.equalsIgnoreCase("Urdu") ? 
                "کیا آپ واقعی اس خریداری کو ریورس کرنا چاہتے ہیں؟" : 
                "Are you sure you want to reverse this purchase?", 
                currentLanguage.equalsIgnoreCase("Urdu") ? "تصدیق کریں" : "Confirm Reverse", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = db.getConnection()) {
                    conn.setAutoCommit(false);
                    
                    // Mark purchase as reversed.
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE purchases SET reversed = 1 WHERE purchase_id = ?")) {
                        stmt.setInt(1, purchaseId);
                        stmt.executeUpdate();
                    }
                    // Mark deals as reversed.
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE deals SET reversed = 1 WHERE purchase_id = ?")) {
                        stmt.setInt(1, purchaseId);
                        stmt.executeUpdate();
                    }
                    // Deduct stock.
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE inventory SET stock = stock - (SELECT quantity FROM purchases WHERE purchase_id = ?) " +
                            "WHERE item_id = (SELECT item_id FROM purchases WHERE purchase_id = ?)")) {
                        stmt.setInt(1, purchaseId);
                        stmt.setInt(2, purchaseId);
                        stmt.executeUpdate();
                    }
                    // Reduce supplier credit if payment was on account.
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE suppliers SET balance = balance - (SELECT total_price FROM purchases WHERE purchase_id = ?) " +
                            "WHERE supplier_id = (SELECT supplier_id FROM purchases WHERE purchase_id = ?) " +
                            "AND EXISTS (SELECT 1 FROM purchases WHERE purchase_id = ? AND payment_type = 'account')")) {
                        stmt.setInt(1, purchaseId);
                        stmt.setInt(2, purchaseId);
                        stmt.setInt(3, purchaseId);
                        stmt.executeUpdate();
                    }
                    conn.commit();
                    JOptionPane.showMessageDialog(this, 
                        currentLanguage.equalsIgnoreCase("Urdu") ? "خریداری کامیابی سے ریورس ہو گئی ہے!" : "Purchase reversed successfully!", 
                        currentLanguage.equalsIgnoreCase("Urdu") ? "کامیابی" : "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadPurchasesData();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, 
                        currentLanguage.equalsIgnoreCase("Urdu") ? "خریداری ریورس کرنے میں خرابی!" : "Error reversing purchase!", 
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
        // Update button texts, fonts, and table column headers based on currentLanguage
        if (currentLanguage.equalsIgnoreCase("Urdu")) {
            createPurchaseButton.setText("خریداری بنائیں");
            reversePurchaseButton.setText("خریداری ریورس کریں");
            refreshButton.setText("ریفریش");
            tableModel.setColumnIdentifiers(new String[]{
                "خریداری آئی ڈی", "آئٹم کا نام", "بارکوڈ", "سپلائر آئی ڈی", "مقدار", "کل قیمت", "خریداری کی تاریخ", "ادائیگی کی قسم", "الٹ دیا گیا"
            });
            Font urduFont = new Font("Jameel Noori Nastaleeq", Font.BOLD, 18);
            createPurchaseButton.setFont(urduFont);
            reversePurchaseButton.setFont(urduFont);
            refreshButton.setFont(urduFont);
            purchasesTable.getTableHeader().setFont(urduFont.deriveFont(urduFont.getSize2D() * 4 / 3f));
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        } else {
            createPurchaseButton.setText("Create Purchase");
            reversePurchaseButton.setText("Reverse Purchase");
            refreshButton.setText("Refresh");
            tableModel.setColumnIdentifiers(new String[]{
                "Purchase ID", "Item Name", "Barcode", "Supplier ID", "Quantity", "Total Price", "Purchase Date", "Payment Type", "Reversed"
            });
            Font englishFont = new Font("Arial", Font.BOLD, 18);
            createPurchaseButton.setFont(englishFont);
            reversePurchaseButton.setFont(englishFont);
            refreshButton.setFont(englishFont);
            purchasesTable.getTableHeader().setFont(englishFont.deriveFont(englishFont.getSize2D() * 4 / 3f));
            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        }
        revalidate();
        repaint();
    }
}
