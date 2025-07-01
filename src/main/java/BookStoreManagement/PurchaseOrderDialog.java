package BookStoreManagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderDialog extends JDialog {
    private static final long SCANNER_THRESHOLD_MS = 50;

    private JTextField itemNameField, barcodeField, quantityField, totalPriceField, supplierNameField, supplierPhoneField;
    private JComboBox<String> conditionComboBox, paymentTypeComboBox;
    private JButton addItemButton, previousButton, proceedButton;
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private JLabel pricePerUnitLabel;
    private Database db;
    private List<Object[]> cart;
    private long lastKeyPressTime = 0;

    // Panels for grouping components (for orientation)
    private JPanel inputPanel;
    private JPanel buttonPanel;
    private JPanel bottomPanel;

    public PurchaseOrderDialog(Frame parent, Database db) {
        super(parent, "Create Purchase Order", true);
        this.db = db;
        this.cart = new ArrayList<>();
        setSize(600, 500);
        setLayout(new BorderLayout());

        // =================== Input Fields Panel ===================
        inputPanel = new JPanel(new GridLayout(9, 2, 5, 5)); // 9 rows

        // Row 1: Item Name
        inputPanel.add(new JLabel("Item Name:"));
        itemNameField = new JTextField();
        itemNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                fetchItemDetailsByItemName();
            }
        });
        inputPanel.add(itemNameField);

        // Row 2: Barcode
        inputPanel.add(new JLabel("Barcode:"));
        barcodeField = new JTextField();
        barcodeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastKeyPressTime < SCANNER_THRESHOLD_MS && !barcodeField.getText().trim().isEmpty()) {
                    fetchItemDetailsByBarcode();
                    addItemToCart(true); // auto-add with quantity = 1
                    clearFields();
                }
                lastKeyPressTime = currentTime;
            }
        });
        barcodeField.addActionListener(e -> {
            fetchItemDetailsByBarcode();
            addItemToCart(false);
        });
        inputPanel.add(barcodeField);

        // Row 3: Quantity
        inputPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        quantityField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updatePricePerUnit();
            }
        });
        inputPanel.add(quantityField);

        // Row 4: Total Price
        inputPanel.add(new JLabel("Total Price:"));
        totalPriceField = new JTextField();
        totalPriceField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updatePricePerUnit();
            }
        });
        inputPanel.add(totalPriceField);

        // Row 5: Condition
        inputPanel.add(new JLabel("Condition:"));
        conditionComboBox = new JComboBox<>(new String[]{"New", "Like New", "Slightly Used", "Used"});
        conditionComboBox.addActionListener(e -> {
            if (!itemNameField.getText().trim().isEmpty()) {
                fetchItemDetailsByItemName();
            } else if (!barcodeField.getText().trim().isEmpty()) {
                fetchItemDetailsByBarcode();
            }
        });
        inputPanel.add(conditionComboBox);

        // Row 6: Supplier Name
        inputPanel.add(new JLabel("Supplier Name:"));
        supplierNameField = new JTextField();
        inputPanel.add(supplierNameField);

        // Row 7: Supplier Phone
        inputPanel.add(new JLabel("Supplier Phone:"));
        supplierPhoneField = new JTextField();
        inputPanel.add(supplierPhoneField);

        // Row 8: Payment Type
        inputPanel.add(new JLabel("Payment Type:"));
        paymentTypeComboBox = new JComboBox<>(new String[]{"Cash", "Account"});
        inputPanel.add(paymentTypeComboBox);

        // =================== Buttons Panel ===================
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        addItemButton = new JButton("Add Item to Cart");
        addItemButton.addActionListener(e -> addItemToCart(false));
        buttonPanel.add(addItemButton);

        previousButton = new JButton("Previous Item");
        previousButton.addActionListener(e -> goBackToPreviousItem());
        previousButton.setVisible(false);
        buttonPanel.add(previousButton);

        proceedButton = new JButton("Proceed");
        proceedButton.addActionListener(e -> proceedToInvoice());
        buttonPanel.add(proceedButton);

        // =================== Cart Table ===================
        cartTableModel = new DefaultTableModel(new String[]{"Item Name", "Barcode", "Quantity", "Price per Unit", "Total Price", "Condition"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2 || column == 3;
            }
        };
        cartTable = new JTable(cartTableModel);
        JScrollPane cartScrollPane = new JScrollPane(cartTable);

        // =================== Bottom Panel ===================
        bottomPanel = new JPanel(new BorderLayout());
        pricePerUnitLabel = new JLabel("Price per Unit: 0.00");
        bottomPanel.add(buttonPanel, BorderLayout.WEST);
        bottomPanel.add(pricePerUnitLabel, BorderLayout.EAST);

        // =================== Add Panels to Dialog ===================
        add(inputPanel, BorderLayout.NORTH);
        add(cartScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Set default button so Enter triggers addItemToCart.
        getRootPane().setDefaultButton(addItemButton);

        // Apply language settings (one-time, since dialog is modal)
        applyLanguage();
    }

    // --------------------- Custom Font Loader ---------------------
    private Font getCustomUrduFont() {
        try {
            // Ensure the font file is available in your resources at /fonts/JameelNooriNastaleeq.ttf
            InputStream is = getClass().getResourceAsStream("/fonts/JameelNooriNastaleeq.ttf");
            if (is == null) {
                System.err.println("Custom Urdu font file not found!");
                return new Font("Serif", Font.PLAIN, 16);
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            return font.deriveFont(Font.PLAIN, 16);
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("Serif", Font.PLAIN, 16);
        }
    }

    // --------------------- applyLanguage() ---------------------
    public void applyLanguage() {
        if (db.getLanguage().equalsIgnoreCase("Urdu")) {
            // Set title and RIGHT_TO_LEFT orientation for Urdu.
            setTitle("خریداری آرڈر بنائیں");
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            inputPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            buttonPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            bottomPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

            // Update input panel labels based on fixed component order.
            ((JLabel) inputPanel.getComponent(0)).setText("آئٹم کا نام:");
            ((JLabel) inputPanel.getComponent(2)).setText("بارکوڈ:");
            ((JLabel) inputPanel.getComponent(4)).setText("مقدار:");
            ((JLabel) inputPanel.getComponent(6)).setText("کل قیمت:");
            ((JLabel) inputPanel.getComponent(8)).setText("حالت:");
            ((JLabel) inputPanel.getComponent(10)).setText("سپلائر کا نام:");
            ((JLabel) inputPanel.getComponent(12)).setText("سپلائر فون:");
            ((JLabel) inputPanel.getComponent(14)).setText("ادائیگی کی قسم:");

            // Update button texts.
            addItemButton.setText("آئٹم کارٹ میں شامل کریں");
            previousButton.setText("پچھلا آئٹم");
            proceedButton.setText("آگے بڑھیں");

            // Update price per unit label.
            pricePerUnitLabel.setText("یونٹ قیمت: 0.00");

            // Load custom Urdu font
            Font urduFont = getCustomUrduFont();

            // Apply custom font to text fields.
            itemNameField.setFont(urduFont);
            barcodeField.setFont(urduFont);
            quantityField.setFont(urduFont);
            totalPriceField.setFont(urduFont);
            supplierNameField.setFont(urduFont);
            supplierPhoneField.setFont(urduFont);
        } else {
            // For English: set title, orientation, and fonts to Arial.
            setTitle("Create Purchase Order");
            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            inputPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            buttonPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            bottomPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

            ((JLabel) inputPanel.getComponent(0)).setText("Item Name:");
            ((JLabel) inputPanel.getComponent(2)).setText("Barcode:");
            ((JLabel) inputPanel.getComponent(4)).setText("Quantity:");
            ((JLabel) inputPanel.getComponent(6)).setText("Total Price:");
            ((JLabel) inputPanel.getComponent(8)).setText("Condition:");
            ((JLabel) inputPanel.getComponent(10)).setText("Supplier Name:");
            ((JLabel) inputPanel.getComponent(12)).setText("Supplier Phone:");
            ((JLabel) inputPanel.getComponent(14)).setText("Payment Type:");

            addItemButton.setText("Add Item to Cart");
            previousButton.setText("Previous Item");
            proceedButton.setText("Proceed");

            pricePerUnitLabel.setText("Price per Unit: 0.00");

            Font englishFont = new Font("Arial", Font.PLAIN, 16);
            itemNameField.setFont(englishFont);
            barcodeField.setFont(englishFont);
            quantityField.setFont(englishFont);
            totalPriceField.setFont(englishFont);
            supplierNameField.setFont(englishFont);
            supplierPhoneField.setFont(englishFont);
        }
        revalidate();
        repaint();
    }

    // --------------------- Fetch Item Details Methods ---------------------
    private void fetchItemDetailsByItemName() {
        String itemName = itemNameField.getText().trim();
        String condition = (String) conditionComboBox.getSelectedItem();
        if (itemName.isEmpty()) return;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT barcode, purchase_price FROM inventory WHERE item_name = ? AND item_condition = ?")) {
            stmt.setString(1, itemName);
            stmt.setString(2, condition);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                barcodeField.setText(rs.getString("barcode"));
                double purchasePrice = rs.getDouble("purchase_price");
                if (quantityField.getText().trim().isEmpty()) {
                    quantityField.setText("1");
                }
                int quantity = Integer.parseInt(quantityField.getText().trim());
                totalPriceField.setText(String.format("%.2f", purchasePrice * quantity));
                updatePricePerUnit();
            } else {
                barcodeField.setText("");
                totalPriceField.setText("");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching item details!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fetchItemDetailsByBarcode() {
        String barcode = barcodeField.getText().trim();
        String condition = (String) conditionComboBox.getSelectedItem();
        if (barcode.isEmpty()) return;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT item_name, purchase_price FROM inventory WHERE barcode = ? AND item_condition = ?")) {
            stmt.setString(1, barcode);
            stmt.setString(2, condition);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                itemNameField.setText(rs.getString("item_name"));
                double purchasePrice = rs.getDouble("purchase_price");
                if (quantityField.getText().trim().isEmpty()) {
                    quantityField.setText("1");
                }
                int quantity = Integer.parseInt(quantityField.getText().trim());
                totalPriceField.setText(String.format("%.2f", purchasePrice * quantity));
                updatePricePerUnit();
            } else {
                itemNameField.setText("");
                totalPriceField.setText("");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching item details!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --------------------- Price Per Unit ---------------------
    private void updatePricePerUnit() {
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            double totalPrice = Double.parseDouble(totalPriceField.getText().trim());
            double pricePerUnit = totalPrice / quantity;
            pricePerUnitLabel.setText(String.format("Price per Unit: %.2f", pricePerUnit));
        } catch (NumberFormatException ignored) {
            pricePerUnitLabel.setText("Price per Unit: 0.00");
        }
    }

    // --------------------- Add Item to Cart ---------------------
    private void addItemToCart(boolean autoAdd) {
        String itemName = itemNameField.getText().trim();
        String barcode = barcodeField.getText().trim();
        String condition = (String) conditionComboBox.getSelectedItem();
        int quantity;
        double unitPrice;

        if (autoAdd) {
            quantity = 1;
        } else {
            String quantityText = quantityField.getText().trim();
            if (quantityText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a quantity!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                quantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid quantity!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String totalPriceText = totalPriceField.getText().trim();
        if (totalPriceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Total Price is required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            double totalPriceVal = Double.parseDouble(totalPriceText);
            unitPrice = totalPriceVal / (autoAdd ? 1 : quantity);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid total price!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (itemName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Item Name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if item already exists in the cart (by item name, barcode, condition)
        boolean itemExists = false;
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            String existingItemName = (String) cartTableModel.getValueAt(i, 0);
            String existingBarcode = (String) cartTableModel.getValueAt(i, 1);
            String existingCondition = (String) cartTableModel.getValueAt(i, 5);
            if (existingItemName.equals(itemName) && existingBarcode.equals(barcode) && existingCondition.equals(condition)) {
                int existingQuantity = Integer.parseInt(cartTableModel.getValueAt(i, 2).toString());
                int newQuantity = existingQuantity + quantity;
                cartTableModel.setValueAt(newQuantity, i, 2);
                double newTotalPrice = newQuantity * unitPrice;
                cartTableModel.setValueAt(String.format("%.2f", unitPrice), i, 3);
                cartTableModel.setValueAt(String.format("%.2f", newTotalPrice), i, 4);
                itemExists = true;
                break;
            }
        }
        if (!itemExists) {
            double totalPriceForItem = unitPrice * quantity;
            cartTableModel.addRow(new Object[]{
                itemName,
                barcode,
                quantity,
                String.format("%.2f", unitPrice),
                String.format("%.2f", totalPriceForItem),
                condition
            });
        }
        recalcTotalPrice();
        if (!autoAdd) {
            clearFields();
        }
        barcodeField.requestFocus();
    }

    private void addItemToCart() {
        addItemToCart(false);
        barcodeField.requestFocus();
    }

    private void recalcTotalPrice() {
        double total = 0;
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            try {
                int quantity = Integer.parseInt(cartTableModel.getValueAt(i, 2).toString());
                double pricePerUnit = Double.parseDouble(cartTableModel.getValueAt(i, 3).toString());
                total += quantity * pricePerUnit;
            } catch (NumberFormatException ex) {
                // Skip row if conversion fails
            }
        }
        // Optionally, update an overall total label.
    }

    private void clearFields() {
        itemNameField.setText("");
        barcodeField.setText("");
        quantityField.setText("");
        totalPriceField.setText("");
    }

    private void goBackToPreviousItem() {
        // Optional: implement if needed.
    }

    private void proceedToInvoice() {
        if (cartTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Add at least one item to the cart!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<Object[]> cart = new ArrayList<>();
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            String itemName = (String) cartTableModel.getValueAt(i, 0);
            String barcode = (String) cartTableModel.getValueAt(i, 1);
            int quantity = Integer.parseInt(cartTableModel.getValueAt(i, 2).toString());
            double salePrice = Double.parseDouble(cartTableModel.getValueAt(i, 3).toString());
            double totalPrice = Double.parseDouble(cartTableModel.getValueAt(i, 4).toString());
            String condition = (String) cartTableModel.getValueAt(i, 5);
            cart.add(new Object[]{itemName, barcode, quantity, salePrice, totalPrice, condition});
        }
        String paymentType = (String) paymentTypeComboBox.getSelectedItem();
        String supplierName = supplierNameField.getText().trim();
        String supplierPhone = supplierPhoneField.getText().trim();

        // Use the overloaded constructor which supplies the current date automatically.
        PurchaseInvoiceDialog invoiceDialog = new PurchaseInvoiceDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                db,
                cart,
                supplierName,
                supplierPhone,
                paymentType
        );
        invoiceDialog.setVisible(true);
    }
}
