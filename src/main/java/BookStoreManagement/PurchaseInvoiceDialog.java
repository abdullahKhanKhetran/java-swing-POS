package BookStoreManagement;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;

public class PurchaseInvoiceDialog extends JDialog {
    private JTextArea invoiceArea;
    private JButton printButton, recordPurchaseButton, exportToPdfButton, closeButton;
    private Database db;
    private List<Object[]> cart;
    private String supplierName;
    private String supplierPhone;
    private String paymentType;
    // Fixed: use java.awt.Font and initialize properly
    private Font urduFont = new Font("Jameel Noori Nastaleeq", Font.PLAIN, 12);

    // Cart row structure:
    // [0] = itemName, [1] = barcode, [2] = quantity, [3] = price per unit, [4] = total price, [5] = condition
    public PurchaseInvoiceDialog(Frame parent, Database db, List<Object[]> cart,
                                 String supplierName, String supplierPhone, String paymentType) {
        super(parent, "Purchase Invoice", true);
        this.db = db;
        this.cart = cart;
        this.supplierName = supplierName;
        this.supplierPhone = supplierPhone;
        this.paymentType = paymentType;
        setSize(500, 600);
        setLayout(new BorderLayout());

        // Invoice Text Area
        invoiceArea = new JTextArea();
        invoiceArea.setEditable(false);
        // Default font for English; will be updated in applyLanguage()
        invoiceArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(invoiceArea);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        printButton = new JButton("Print Invoice");
        printButton.addActionListener(e -> printInvoice());
        buttonPanel.add(printButton);

        recordPurchaseButton = new JButton("Record Purchase");
        recordPurchaseButton.addActionListener(e -> recordPurchase());
        buttonPanel.add(recordPurchaseButton);

        exportToPdfButton = new JButton("Export to PDF");
        exportToPdfButton.addActionListener(e -> exportToPdf());
        buttonPanel.add(exportToPdfButton);

        closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Load invoice data and apply language settings
        loadInvoiceData();
        applyLanguage();
    }

    private void loadInvoiceData() {
        StringBuilder invoiceText = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (db.getLanguage().equalsIgnoreCase("Urdu")) {
            invoiceText.append("========================================\n");
            invoiceText.append("              خریداری رسید              \n");
            invoiceText.append("========================================\n");
            invoiceText.append(String.format("%-20s: %s\n", "سپلائر کا نام", supplierName));
            invoiceText.append(String.format("%-20s: %s\n", "سپلائر فون", supplierPhone));
            invoiceText.append(String.format("%-20s: %s\n", "خریداری کی تاریخ", dateFormat.format(new Date())));
            invoiceText.append(String.format("%-20s: %s\n", "ادائیگی کی قسم", paymentType));
            invoiceText.append("========================================\n");
            invoiceText.append(String.format("%-20s %-10s %-15s %-15s %-15s\n",
                                             "آئٹم کا نام", "مقدار", "یونٹ قیمت", "کل قیمت", "حالت"));
            invoiceText.append("========================================\n");
        } else {
            invoiceText.append("========================================\n");
            invoiceText.append("              PURCHASE INVOICE          \n");
            invoiceText.append("========================================\n");
            invoiceText.append(String.format("%-20s: %s\n", "Supplier Name", supplierName));
            invoiceText.append(String.format("%-20s: %s\n", "Supplier Phone", supplierPhone));
            invoiceText.append(String.format("%-20s: %s\n", "Purchase Date", dateFormat.format(new Date())));
            invoiceText.append(String.format("%-20s: %s\n", "Payment Type", paymentType));
            invoiceText.append("========================================\n");
            invoiceText.append(String.format("%-20s %-10s %-15s %-15s %-15s\n",
                                             "Item Name", "Quantity", "Price per Unit", "Total Price", "Condition"));
            invoiceText.append("========================================\n");
        }

        double totalInvoicePrice = 0;
        for (Object[] item : cart) {
            String itemName = (String) item[0];
            int quantity = (int) item[2];
            double pricePerUnit = (double) item[3];
            double totalPrice = (double) item[4];
            String condition = (String) item[5];

            invoiceText.append(String.format("%-20s %-10d %-15.2f %-15.2f %-15s\n",
                                             itemName, quantity, pricePerUnit, totalPrice, condition));
            totalInvoicePrice += totalPrice;
        }

        if (db.getLanguage().equalsIgnoreCase("Urdu")) {
            invoiceText.append("========================================\n");
            invoiceText.append(String.format("%-20s: %.2f\n", "کل قیمت", totalInvoicePrice));
            invoiceText.append("========================================\n");
        } else {
            invoiceText.append("========================================\n");
            invoiceText.append(String.format("%-20s: %.2f\n", "Total Price", totalInvoicePrice));
            invoiceText.append("========================================\n");
        }

        invoiceArea.setText(invoiceText.toString());
    }

    private void printInvoice() {
        try {
            invoiceArea.print();
        } catch (PrinterException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error printing invoice!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recordPurchase() {
        try (Connection conn = db.getConnection()) {
            int supplierId = getSupplierId(conn, supplierName, supplierPhone);
            if (supplierId == -1) {
                supplierId = insertSupplier(conn, supplierName, supplierPhone);
                if (supplierId == -1) {
                    JOptionPane.showMessageDialog(this, "Error creating supplier!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            for (Object[] item : cart) {
                String itemName = (String) item[0];
                String barcode = (String) item[1];
                int quantity = (int) item[2];
                double pricePerUnit = (double) item[3];
                double totalPrice = (double) item[4];
                String condition = (String) item[5];

                int itemId = updateOrInsertInventory(conn, itemName, barcode, quantity, pricePerUnit, condition);

                String purchasesQuery = "INSERT INTO purchases (supplier_id, supplier_name, supplier_phone, total_price, " +
                        "item_name, quantity, purchase_price, item_condition, payment_type, item_id, barcode) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(purchasesQuery)) {
                    pstmt.setInt(1, supplierId);
                    pstmt.setString(2, supplierName);
                    pstmt.setString(3, supplierPhone);
                    pstmt.setDouble(4, totalPrice);
                    pstmt.setString(5, itemName);
                    pstmt.setInt(6, quantity);
                    pstmt.setDouble(7, pricePerUnit);
                    pstmt.setString(8, condition);
                    pstmt.setString(9, paymentType);
                    pstmt.setInt(10, itemId);
                    pstmt.setString(11, barcode);
                    pstmt.executeUpdate();
                }
            }
            JOptionPane.showMessageDialog(this, "Purchase recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error recording purchase!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getSupplierId(Connection conn, String supplierName, String supplierPhone) throws SQLException {
        String query = "SELECT supplier_id FROM suppliers WHERE supplier_name = ? AND phone = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, supplierName);
            pstmt.setString(2, supplierPhone);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt("supplier_id") : -1;
        }
    }

    private int insertSupplier(Connection conn, String supplierName, String supplierPhone) throws SQLException {
        String insertQuery = "INSERT INTO suppliers (supplier_name, phone) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, supplierName);
            pstmt.setString(2, supplierPhone);
            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    private int updateOrInsertInventory(Connection conn, String itemName, String barcode, int quantity,
                                        double purchasePrice, String condition) throws SQLException {
        String checkQuery = "SELECT item_id, stock FROM inventory WHERE item_name = ? AND item_condition = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
            pstmt.setString(1, itemName);
            pstmt.setString(2, condition);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int existingStock = rs.getInt("stock");
                int newStock = existingStock + quantity;
                String updateQuery = "UPDATE inventory SET stock = ?, purchase_price = ? WHERE item_id = ?";
                try (PreparedStatement updatePstmt = conn.prepareStatement(updateQuery)) {
                    updatePstmt.setInt(1, newStock);
                    updatePstmt.setDouble(2, purchasePrice);
                    updatePstmt.setInt(3, rs.getInt("item_id"));
                    updatePstmt.executeUpdate();
                }
                return rs.getInt("item_id");
            } else {
                NewItemInfo info = showNewItemDialog(itemName);
                if (info == null) {
                    return -1;
                }
                String insertQuery = "INSERT INTO inventory (item_name, barcode, stock, purchase_price, item_condition, category, subcategory, company) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertPstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                    insertPstmt.setString(1, itemName);
                    insertPstmt.setString(2, barcode);
                    insertPstmt.setInt(3, quantity);
                    insertPstmt.setDouble(4, purchasePrice);
                    insertPstmt.setString(5, condition);
                    insertPstmt.setString(6, info.getCategory());
                    insertPstmt.setString(7, info.getSubcategory());
                    insertPstmt.setString(8, info.getCompany());
                    insertPstmt.executeUpdate();
                    ResultSet keys = insertPstmt.getGeneratedKeys();
                    return keys.next() ? keys.getInt(1) : -1;
                }
            }
        }
    }

    private NewItemInfo showNewItemDialog(String itemName) {
        JTextField categoryField = new JTextField();
        JTextField subcategoryField = new JTextField();
        JTextField companyField = new JTextField();

        Object[] message = {
            "New Item: " + itemName,
            "Category:", categoryField,
            "Subcategory:", subcategoryField,
            "Company:", companyField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Enter New Item Details", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String category = categoryField.getText().trim();
            String subcategory = subcategoryField.getText().trim();
            String company = companyField.getText().trim();
            if (category.isEmpty() || subcategory.isEmpty() || company.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required for a new item!", "Error", JOptionPane.ERROR_MESSAGE);
                return showNewItemDialog(itemName); // Re-prompt
            }
            return new NewItemInfo(category, subcategory, company);
        } else {
            return null;
        }
    }

    // Inner class to hold additional new item details
    private class NewItemInfo {
        private String category;
        private String subcategory;
        private String company;

        public NewItemInfo(String category, String subcategory, String company) {
            this.category = category;
            this.subcategory = subcategory;
            this.company = company;
        }
        public String getCategory()   { return category; }
        public String getSubcategory(){ return subcategory; }
        public String getCompany()    { return company; }
    }

    private void exportToPdf() {
        // Ask the user which language they prefer for the PDF.
        String[] options = {"English", "Urdu"};
        int choice = JOptionPane.showOptionDialog(this, "Select PDF language", "PDF Language",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        String pdfLanguage = (choice == 1) ? "Urdu" : "English";

        // Let the user choose the file location
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(pdfLanguage.equals("Urdu") ? "رسید محفوظ کریں" : "Save Purchase Invoice");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".pdf")) {
                    filePath += ".pdf";
                }

                // Create the PDF document using iText 7 APIs.
                PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc);

                // Prepare a date string for the invoice.
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String purchaseDate = LocalDateTime.now().format(formatter);

                // Header
                if (pdfLanguage.equals("Urdu")) {
                    document.add(new Paragraph("========================================"));
                    document.add(new Paragraph("              خریداری رسید              ").setBold());
                    document.add(new Paragraph("========================================"));
                    document.add(new Paragraph("سپلائر کا نام    : " + supplierName));
                    document.add(new Paragraph("سپلائر فون       : " + supplierPhone));
                    document.add(new Paragraph("خریداری کی تاریخ : " + purchaseDate));
                    document.add(new Paragraph("========================================"));
                    document.add(new Paragraph(String.format("%-15s %-10s %-10s %-10s", "آئٹم کا نام", "مقدار", "قیمت فی یونٹ", "کل"))
                            /* PDF font support for Urdu would need a PdfFont here */);
                } else {
                    document.add(new Paragraph("========================================"));
                    document.add(new Paragraph("              PURCHASE INVOICE              ").setBold());
                    document.add(new Paragraph("========================================"));
                    document.add(new Paragraph("Supplier Name    : " + supplierName));
                    document.add(new Paragraph("Supplier Phone   : " + supplierPhone));
                    document.add(new Paragraph("Purchase Date    : " + purchaseDate));
                    document.add(new Paragraph("========================================"));
                    document.add(new Paragraph(String.format("%-15s %-10s %-10s %-10s", "Item Name", "Quantity", "Unit Price", "Total")));
                }
                document.add(new Paragraph("========================================"));

                // Table of items
                double totalPrice = 0;
                for (Object[] item : cart) {
                    String itemName = (String) item[0];
                    int quantity = (int) item[2];
                    double unitPrice = (double) item[3];
                    double lineTotal = unitPrice * quantity;
                    totalPrice += lineTotal;
                    document.add(new Paragraph(String.format("%-15s %-10d %-10.2f %-10.2f",
                            itemName, quantity, unitPrice, lineTotal)));
                }

                document.add(new Paragraph("========================================"));
                if (pdfLanguage.equals("Urdu")) {
                    document.add(new Paragraph(String.format("کل قیمت   : PKR %.2f", totalPrice)));
                } else {
                    document.add(new Paragraph(String.format("Total Price: PKR %.2f", totalPrice)));
                }
                document.add(new Paragraph("========================================"));

                document.close();
                JOptionPane.showMessageDialog(this,
                        (pdfLanguage.equals("Urdu")
                                ? "رسید کامیابی سے محفوظ ہو گئی:\n"
                                : "Invoice saved successfully at:\n") + filePath,
                        "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error exporting to PDF!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ----------------- Bilingual Support: applyLanguage() -----------------
    public void applyLanguage() {
        if (db.getLanguage().equalsIgnoreCase("Urdu")) {
            setTitle("خریداری رسید");
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            invoiceArea.setFont(new Font("Jameel Noori Nastaleeq", Font.PLAIN, 12));
            printButton.setText("رسید پرنٹ کریں");
            recordPurchaseButton.setText("خریداری ریکارڈ کریں");
            exportToPdfButton.setText("PDF ایکسپورٹ کریں");
            closeButton.setText("بند کریں");
        } else {
            setTitle("Purchase Invoice");
            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            invoiceArea.setFont(new Font("Arial", Font.PLAIN, 12));
            printButton.setText("Print Invoice");
            recordPurchaseButton.setText("Record Purchase");
            exportToPdfButton.setText("Export to PDF");
            closeButton.setText("Close");
        }
        loadInvoiceData();
        revalidate();
        repaint();
    }
}
