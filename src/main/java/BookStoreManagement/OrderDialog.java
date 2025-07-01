package BookStoreManagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDialog extends JDialog {
    
    // Fields for item details and customer information
    private JTextField itemNameField, barcodeField, quantityField, salePriceField, discountField;
    private JTextField customerNameField, customerPhoneField;
    private JComboBox<String> conditionCombo;
    
    // Payment type radio buttons and grouping
    private JRadioButton cashRadio, accountRadio;
    private ButtonGroup paymentGroup;
    
    // Cart table and its model
    private JTable orderTable;
    private DefaultTableModel tableModel;
    
    // Buttons for adding items and proceeding to invoice
    private JButton addToCartButton, proceedButton;
    
    // Label to show the overall cart total price (after discount)
    private JLabel totalPriceLabel;
    
    // Reference to database object for fetching item details
    private Database db;
    
    // Fonts for bilingual support
    private Font englishFont = new Font("Arial", Font.PLAIN, 16);
    private Font urduFont = loadUrduFont();
    
    public OrderDialog(JFrame parent, Database db) {
        super(parent, db.getLanguage().equalsIgnoreCase("Urdu") ? "آرڈر بنائیں" : "Create Order", true);
        this.db = db;
        
        // Set orientation based on language
        if (db.getLanguage().equalsIgnoreCase("Urdu")) {
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        } else {
            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        }
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        String lang = db.getLanguage();
        
        // ===================== Row 0: Item Name & Barcode =====================
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel itemNameLabel = new JLabel(lang.equalsIgnoreCase("Urdu") ? "آئٹم کا نام:" : "Item Name:");
        itemNameLabel.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(itemNameLabel, gbc);
        
        gbc.gridx = 1;
        itemNameField = new JTextField(15);
        itemNameField.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(itemNameField, gbc);
        itemNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                fetchItemDetailsByItemName();
            }
        });
        
        gbc.gridx = 2;
        JLabel barcodeLabel = new JLabel(lang.equalsIgnoreCase("Urdu") ? "بارکوڈ:" : "Barcode:");
        barcodeLabel.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(barcodeLabel, gbc);
        
        gbc.gridx = 3;
        barcodeField = new JTextField(15);
        barcodeField.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(barcodeField, gbc);
        barcodeField.addKeyListener(new KeyAdapter() {
            private long lastKeyPressTime = 0;
            @Override
            public void keyReleased(KeyEvent e) {
                long now = System.currentTimeMillis();
                if (now - lastKeyPressTime < 20 && !barcodeField.getText().trim().isEmpty()) {
                    fetchItemDetailsByBarcode();
                    addItemToCart(true);
                    clearFields();
                }
                lastKeyPressTime = now;
            }
        });
        barcodeField.addActionListener(e -> {
            fetchItemDetailsByBarcode();
            addItemToCart(false);
        });
        
        // ===================== Row 1: Quantity & Sale Price =====================
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel quantityLabel = new JLabel(lang.equalsIgnoreCase("Urdu") ? "مقدار:" : "Quantity:");
        quantityLabel.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(quantityLabel, gbc);
        
        gbc.gridx = 1;
        quantityField = new JTextField(5);
        quantityField.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(quantityField, gbc);
        quantityField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateTotalPrice();
            }
        });
        
        gbc.gridx = 2;
        JLabel salePriceLabel = new JLabel(lang.equalsIgnoreCase("Urdu") ? "فروخت قیمت:" : "Sale Price:");
        salePriceLabel.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(salePriceLabel, gbc);
        
        gbc.gridx = 3;
        salePriceField = new JTextField(10);
        salePriceField.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(salePriceField, gbc);
        
        // ===================== Row 2: Discount Percentage =====================
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel discountLabel = new JLabel(lang.equalsIgnoreCase("Urdu") ? "ڈسکاؤنٹ (%):" : "Discount (%):");
        discountLabel.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(discountLabel, gbc);
        
        gbc.gridx = 1;
        discountField = new JTextField(5);
        discountField.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(discountField, gbc);
        discountField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateTotalPrice();
            }
        });
        
        // ===================== Row 3: Condition =====================
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel conditionLabel = new JLabel(lang.equalsIgnoreCase("Urdu") ? "حالت:" : "Condition:");
        conditionLabel.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(conditionLabel, gbc);
        
        gbc.gridx = 1;
        conditionCombo = new JComboBox<>(lang.equalsIgnoreCase("Urdu")
            ? new String[]{"نیا", "استعمال شدہ"}
            : new String[]{"New", "Used"});
        conditionCombo.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(conditionCombo, gbc);
        conditionCombo.addActionListener(e -> {
            if (!itemNameField.getText().trim().isEmpty()) {
                fetchItemDetailsByItemName();
            } else if (!barcodeField.getText().trim().isEmpty()) {
                fetchItemDetailsByBarcode();
            }
        });
        
        // ===================== Row 4: Payment Type =====================
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel paymentTypeLabel = new JLabel(lang.equalsIgnoreCase("Urdu") ? "ادائیگی کی قسم:" : "Payment Type:");
        paymentTypeLabel.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(paymentTypeLabel, gbc);
        
        gbc.gridx = 1;
        cashRadio = new JRadioButton(lang.equalsIgnoreCase("Urdu") ? "کیش" : "Cash", true);
        accountRadio = new JRadioButton(lang.equalsIgnoreCase("Urdu") ? "اکاؤنٹ" : "Account");
        cashRadio.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        accountRadio.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        paymentGroup = new ButtonGroup();
        paymentGroup.add(cashRadio);
        paymentGroup.add(accountRadio);
        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        paymentPanel.setComponentOrientation(lang.equalsIgnoreCase("Urdu") 
            ? ComponentOrientation.RIGHT_TO_LEFT 
            : ComponentOrientation.LEFT_TO_RIGHT);
        paymentPanel.add(cashRadio);
        paymentPanel.add(accountRadio);
        add(paymentPanel, gbc);
        
        cashRadio.addActionListener(e -> toggleCustomerFields(false));
        accountRadio.addActionListener(e -> toggleCustomerFields(true));
        
        // ===================== Row 5: Customer Name & Phone =====================
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel customerNameLabel = new JLabel(lang.equalsIgnoreCase("Urdu") ? "کسٹمر کا نام:" : "Customer Name:");
        customerNameLabel.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(customerNameLabel, gbc);
        
        gbc.gridx = 1;
        customerNameField = new JTextField(15);
        customerNameField.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(customerNameField, gbc);
        
        gbc.gridx = 2;
        JLabel customerPhoneLabel = new JLabel(lang.equalsIgnoreCase("Urdu") ? "کسٹمر فون:" : "Customer Phone:");
        customerPhoneLabel.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(customerPhoneLabel, gbc);
        
        gbc.gridx = 3;
        customerPhoneField = new JTextField(10);
        customerPhoneField.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(customerPhoneField, gbc);
        toggleCustomerFields(false);
        
        // ===================== Row 6: Order Table =====================
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 4;
        tableModel = new DefaultTableModel(new String[]{
            lang.equalsIgnoreCase("Urdu") ? "آئٹم کا نام"     : "Item Name",
            lang.equalsIgnoreCase("Urdu") ? "بارکوڈ"          : "Barcode",
            lang.equalsIgnoreCase("Urdu") ? "مقدار"           : "Quantity",
            lang.equalsIgnoreCase("Urdu") ? "فروخت قیمت"      : "Sale Price",
            lang.equalsIgnoreCase("Urdu") ? "ڈسکاؤنٹ (%)"     : "Discount (%)",
            lang.equalsIgnoreCase("Urdu") ? "کل قیمت"         : "Total Price",
            lang.equalsIgnoreCase("Urdu") ? "حالت"            : "Condition"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2 || column == 3 || column == 4;
            }
        };
        orderTable = new JTable(tableModel);
        orderTable.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        JScrollPane tableScroll = new JScrollPane(orderTable);
        tableScroll.setPreferredSize(new Dimension(450, 120));
        add(tableScroll, gbc);
        
        // ===================== Row 7: Total Cart Price Label =====================
        gbc.gridy++;
        gbc.gridx = 2; gbc.gridwidth = 2;
        totalPriceLabel = new JLabel(lang.equalsIgnoreCase("Urdu")
            ? "کل قیمت: PKR 0.00"
            : "Total Cart Price: PKR 0.00",
            SwingConstants.RIGHT);
        totalPriceLabel.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(totalPriceLabel, gbc);
        
        // ===================== Row 8: Buttons =====================
        gbc.gridy++;
        gbc.gridwidth = 2; gbc.gridx = 0;
        addToCartButton = new JButton(lang.equalsIgnoreCase("Urdu")
            ? "آئٹم کارٹ میں شامل کریں"
            : "Add Item to Cart");
        addToCartButton.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(addToCartButton, gbc);
        addToCartButton.addActionListener(e -> addItemToCart(false));
        
        gbc.gridx = 2; gbc.gridwidth = 2;
        proceedButton = new JButton(lang.equalsIgnoreCase("Urdu") ? "آگے بڑھیں" : "Proceed");
        proceedButton.setFont(lang.equalsIgnoreCase("Urdu") ? urduFont : englishFont);
        add(proceedButton, gbc);
        proceedButton.addActionListener(e -> proceedToInvoice());
        
        pack();
        setLocationRelativeTo(parent);
        getRootPane().setDefaultButton(addToCartButton);
    }
    
    // Utility: load embedded Urdu font
    private Font loadUrduFont() {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/fonts/JameelNooriNastaleeq.ttf");
            if (is != null) {
                Font f = Font.createFont(Font.TRUETYPE_FONT, is);
                return f.deriveFont(Font.PLAIN, 16);
            }
            System.err.println("Urdu font not found, falling back.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Font("Serif", Font.PLAIN, 16);
    }
    
    private void fetchItemDetailsByItemName() {
        String name = itemNameField.getText().trim();
        if (name.isEmpty()) return;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT barcode, sale_price FROM inventory WHERE item_name = ?")) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                barcodeField.setText(rs.getString("barcode"));
                salePriceField.setText(String.format("%.2f", rs.getDouble("sale_price")));
                if (quantityField.getText().trim().isEmpty()) quantityField.setText("1");
                updateTotalPrice();
            } else {
                barcodeField.setText("");
                salePriceField.setText("");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                db.getLanguage().equalsIgnoreCase("Urdu")
                    ? "آئٹم کی تفصیلات لانے میں خرابی!"
                    : "Error fetching item details!",
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void fetchItemDetailsByBarcode() {
        String code = barcodeField.getText().trim();
        if (code.isEmpty()) return;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT item_name, sale_price FROM inventory WHERE barcode = ?")) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                itemNameField.setText(rs.getString("item_name"));
                salePriceField.setText(String.format("%.2f", rs.getDouble("sale_price")));
                if (quantityField.getText().trim().isEmpty()) quantityField.setText("1");
                updateTotalPrice();
            } else {
                itemNameField.setText("");
                salePriceField.setText("");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                db.getLanguage().equalsIgnoreCase("Urdu")
                    ? "آئٹم کی تفصیلات لانے میں خرابی!"
                    : "Error fetching item details!",
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Recalculates the TOTAL of all rows in the cart and updates the label
    private void updateTotalPrice() {
        double total = recalcCartTotal();
        totalPriceLabel.setText(String.format(
            langEqualsUrdu() ? "کل قیمت: PKR %.2f" : "Total Cart Price: PKR %.2f",
            total
        ));
    }
    
    private double recalcCartTotal() {
        double sum = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                sum += Double.parseDouble(tableModel.getValueAt(i, 5).toString());
            } catch (Exception e) {
                // skip invalid rows
            }
        }
        return sum;
    }
    
    private void addItemToCart(boolean autoAdd) {
        String name = itemNameField.getText().trim();
        String code = barcodeField.getText().trim();
        String qtyText = quantityField.getText().trim();
        String priceText = salePriceField.getText().trim();
        String discText = discountField.getText().trim();
        String cond     = (String) conditionCombo.getSelectedItem();
        
        if (name.isEmpty() || qtyText.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                langEqualsUrdu() ? "براہ مہربانی تمام ضروری فیلڈز پُر کریں!" : "Please fill all required fields!",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int qty;
        double price, disc = 0;
        try {
            qty   = Integer.parseInt(qtyText);
            price = Double.parseDouble(priceText);
            if (!discText.isEmpty()) disc = Double.parseDouble(discText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                langEqualsUrdu() ? "غلط نمبر کی شکل!" : "Invalid number format!",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        double discountedPrice = price * (1 - disc/100);
        double lineTotal       = discountedPrice * qty;
        
        // Merge if same item/barcode/condition already in cart
        boolean found = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i,0).equals(name)
             && tableModel.getValueAt(i,1).equals(code)
             && tableModel.getValueAt(i,6).equals(cond)) {
                int oldQty = Integer.parseInt(tableModel.getValueAt(i,2).toString());
                int newQty = oldQty + (autoAdd ? 1 : qty);
                tableModel.setValueAt(newQty, i, 2);
                tableModel.setValueAt(String.format("%.2f", disc), i, 4);
                double newTotal = newQty * price * (1 - disc/100);
                tableModel.setValueAt(String.format("%.2f", newTotal), i, 5);
                found = true;
                break;
            }
        }
        
        if (!found) {
            tableModel.addRow(new Object[]{
                name,
                code,
                autoAdd ? 1 : qty,
                String.format("%.2f", price),
                String.format("%.2f", disc),
                String.format("%.2f", lineTotal),
                cond
            });
        }
        
        updateTotalPrice();
        if (!autoAdd) clearFields();
        barcodeField.requestFocus();
    }
    
    private void clearFields() {
        itemNameField.setText("");
        barcodeField.setText("");
        quantityField.setText("");
        salePriceField.setText("");
        discountField.setText("");
    }
    
    /**
     * Enables or disables the customer fields and sets defaults.
     */
    private void toggleCustomerFields(boolean enable) {
        customerNameField.setEditable(enable);
        customerPhoneField.setEditable(enable);
        if (!enable) {
            customerNameField.setText(langEqualsUrdu() ? "کیش کسٹمر" : "Cash Customer");
            customerPhoneField.setText("N/A");
        } else {
            customerNameField.setText("");
            customerPhoneField.setText("");
        }
    }
    
    private void proceedToInvoice() {
        String custName  = customerNameField.getText().trim();
        String custPhone = customerPhoneField.getText().trim();
        
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                langEqualsUrdu() ? "کارٹ میں کم از کم ایک آئٹم شامل کریں!" : "Add at least one item to the cart!",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (custName.isEmpty() || custPhone.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                langEqualsUrdu() ? "کسٹمر کا نام یا فون خالی نہیں ہو سکتا" : "Customer name or phone can't be empty",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        List<Object[]> cart = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            cart.add(new Object[]{
                tableModel.getValueAt(i,0),
                tableModel.getValueAt(i,1),
                Integer.parseInt(tableModel.getValueAt(i,2).toString()),
                Double.parseDouble(tableModel.getValueAt(i,3).toString()),
                Double.parseDouble(tableModel.getValueAt(i,4).toString()),
                Double.parseDouble(tableModel.getValueAt(i,5).toString()),
                tableModel.getValueAt(i,6)
            });
        }
        
        String paymentType = cashRadio.isSelected()
            ? (langEqualsUrdu() ? "کیش" : "Cash")
            : (langEqualsUrdu() ? "اکاؤنٹ" : "Account");
        
        // Pass off to the InvoiceDialog (make sure its constructor matches these parameters)
        InvoiceDialog invoiceDialog = new InvoiceDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            db,
            cart,
            paymentType,
            custName,
            custPhone
        );
        invoiceDialog.setVisible(true);
    }
    
    private boolean langEqualsUrdu() {
        return db.getLanguage().equalsIgnoreCase("Urdu");
    }
}
