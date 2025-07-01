package BookStoreManagement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.sql.Types;
import java.util.Vector;

public class AddItemPanel extends JPanel implements LanguageChangeListener {
    private Database db;
    private JTextField barcodeField, itemNameField, companyField, stockField, purchasePriceField, salePriceField, unitField; // Renamed manufacturerField to companyField
    private JComboBox<String> categoryComboBox, subcategoryComboBox, conditionComboBox;
    private JButton addCategoryButton, addSubcategoryButton, saveButton, refreshButton, searchButton, clearFieldsButton;
    private JTextArea previewArea;
    private JLabel profitMarginLabel;
    
    // Instance variables for labels (for language updates)
    private JLabel labelBarcode;
    private JLabel labelItemName;
    private JLabel labelCompany; // Renamed labelManufacturer to labelCompany
    private JLabel labelCondition;
    private JLabel labelCategory;
    private JLabel labelSubcategory;
    private JLabel labelStock;
    private JLabel labelPurchasePrice;
    private JLabel labelSalePrice;
    private JLabel labelUnit;
    
    // Define separate font constants for each language.
    // For English, we use Arial; for Urdu, we use Jameel Noori Nastaleeq.
    private final Font englishFont = new Font("Arial", Font.BOLD, 16);
    private final Font urduFont = new Font("Jameel Noori Nastaleeq", Font.BOLD, 16);
    
    // This variable stores the current language ("English" by default)
    private String currentLanguage = "English";
    
    public AddItemPanel(Database db) {
        this.db = db;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        
        // Use currentLanguage to set the initial font.
        Font currentFont = currentLanguage.equalsIgnoreCase("Urdu") ? urduFont : englishFont;
        
        // Barcode Field
        gbc.gridx = 0;
        gbc.gridy = row;
        labelBarcode = new JLabel("Barcode:");
        labelBarcode.setFont(currentFont);
        add(labelBarcode, gbc);
        
        gbc.gridx = 1;
        barcodeField = new JTextField(15);
        barcodeField.setToolTipText("Enter the barcode of the item.");
        barcodeField.setFont(currentFont);
        add(barcodeField, gbc);
        
        row++;
        // Item Name Field
        gbc.gridx = 0;
        gbc.gridy = row;
        labelItemName = new JLabel("Item Name:");
        labelItemName.setFont(currentFont);
        add(labelItemName, gbc);
        
        gbc.gridx = 1;
        itemNameField = new JTextField(15);
        itemNameField.setToolTipText("Enter the name of the item.");
        itemNameField.setFont(currentFont);
        add(itemNameField, gbc);
        
        // Search Button on same row
        gbc.gridx = 2;
        searchButton = new JButton("Search Item");
        searchButton.setToolTipText("Search for an existing item using Barcode or Item Name.");
        searchButton.setFont(currentFont);
        searchButton.addActionListener(e -> searchItem(true));
        add(searchButton, gbc);
        
        row++;
        // New: Company Field (using "company" column)
        gbc.gridx = 0;
        gbc.gridy = row;
        labelCompany = new JLabel("Company:");  // English text
        labelCompany.setFont(currentFont);
        add(labelCompany, gbc);
        
        gbc.gridx = 1;
        companyField = new JTextField(15);
        companyField.setToolTipText("Enter the company of the item.");
        companyField.setFont(currentFont);
        add(companyField, gbc);
        
        row++;
        // Condition ComboBox
        gbc.gridx = 0;
        gbc.gridy = row;
        labelCondition = new JLabel("Condition:");
        labelCondition.setFont(currentFont);
        add(labelCondition, gbc);
        
        gbc.gridx = 1;
        conditionComboBox = new JComboBox<>(new String[]{"New", "Like New", "Slightly Used", "Used"});
        conditionComboBox.setToolTipText("Select the item condition.");
        conditionComboBox.setFont(currentFont);
        add(conditionComboBox, gbc);
        
        row++;
        // Category ComboBox
        gbc.gridx = 0;
        gbc.gridy = row;
        labelCategory = new JLabel("Category:");
        labelCategory.setFont(currentFont);
        add(labelCategory, gbc);
        
        gbc.gridx = 1;
        categoryComboBox = new JComboBox<>();
        categoryComboBox.setToolTipText("Select the item category.");
        categoryComboBox.setFont(currentFont);
        categoryComboBox.addActionListener(e -> loadSubcategories());
        add(categoryComboBox, gbc);
        
        // Add Category Button
        gbc.gridx = 2;
        addCategoryButton = new JButton("Add Category");
        addCategoryButton.setToolTipText("Add a new category.");
        addCategoryButton.setFont(currentFont);
        addCategoryButton.addActionListener(e -> addCategory());
        add(addCategoryButton, gbc);
        
        row++;
        // Subcategory ComboBox
        gbc.gridx = 0;
        gbc.gridy = row;
        labelSubcategory = new JLabel("Subcategory:");
        labelSubcategory.setFont(currentFont);
        add(labelSubcategory, gbc);
        
        gbc.gridx = 1;
        subcategoryComboBox = new JComboBox<>();
        subcategoryComboBox.setToolTipText("Select the item subcategory.");
        subcategoryComboBox.setFont(currentFont);
        add(subcategoryComboBox, gbc);
        
        // Add Subcategory Button
        gbc.gridx = 2;
        addSubcategoryButton = new JButton("Add Subcategory");
        addSubcategoryButton.setToolTipText("Add a new subcategory.");
        addSubcategoryButton.setFont(currentFont);
        addSubcategoryButton.addActionListener(e -> addSubcategory());
        add(addSubcategoryButton, gbc);
        
        row++;
        // Stock Field
        gbc.gridx = 0;
        gbc.gridy = row;
        labelStock = new JLabel("Stock:");
        labelStock.setFont(currentFont);
        add(labelStock, gbc);
        
        gbc.gridx = 1;
        stockField = new JTextField(10);
        stockField.setToolTipText("Enter the stock quantity.");
        stockField.setFont(currentFont);
        add(stockField, gbc);
        
        row++;
        // Purchase Price Field
        gbc.gridx = 0;
        gbc.gridy = row;
        labelPurchasePrice = new JLabel("Purchase Price:");
        labelPurchasePrice.setFont(currentFont);
        add(labelPurchasePrice, gbc);
        
        gbc.gridx = 1;
        purchasePriceField = new JTextField(10);
        purchasePriceField.setToolTipText("Enter the purchase price.");
        purchasePriceField.setFont(currentFont);
        add(purchasePriceField, gbc);
        
        row++;
        // Sale Price Field
        gbc.gridx = 0;
        gbc.gridy = row;
        labelSalePrice = new JLabel("Sale Price:");
        labelSalePrice.setFont(currentFont);
        add(labelSalePrice, gbc);
        
        gbc.gridx = 1;
        salePriceField = new JTextField(10);
        salePriceField.setToolTipText("Enter the sale price.");
        salePriceField.setFont(currentFont);
        add(salePriceField, gbc);
        
        // Profit Margin Label on same row (column 2)
        gbc.gridx = 2;
        profitMarginLabel = new JLabel("Profit Margin: 0%");
        profitMarginLabel.setFont(currentFont);
        add(profitMarginLabel, gbc);
        
        row++;
        // Unit Field
        gbc.gridx = 0;
        gbc.gridy = row;
        labelUnit = new JLabel("Unit (optional):");
        labelUnit.setFont(currentFont);
        add(labelUnit, gbc);
        
        gbc.gridx = 1;
        unitField = new JTextField(15);
        unitField.setToolTipText("Enter the unit of measurement (e.g., kg, liter).");
        unitField.setFont(currentFont);
        add(unitField, gbc);
        
        row++;
        // Preview Area
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 3;
        previewArea = new JTextArea(5, 30);
        previewArea.setEditable(false);
        previewArea.setToolTipText("Preview of the item details.");
        previewArea.setFont(currentFont);
        add(new JScrollPane(previewArea), gbc);
        gbc.gridwidth = 1;
        
        row++;
        // Save Button and Clear Fields Button on same row
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        saveButton = new JButton("Save Item");
        saveButton.setToolTipText("Save the item to the inventory.");
        saveButton.setFont(currentFont);
        saveButton.addActionListener(e -> saveItem());
        add(saveButton, gbc);
        
        gbc.gridx = 2;
        clearFieldsButton = new JButton("Clear Fields");
        clearFieldsButton.setToolTipText("Clear all input fields.");
        clearFieldsButton.setFont(currentFont);
        clearFieldsButton.addActionListener(e -> clearFields());
        add(clearFieldsButton, gbc);
        
        row++;
        // Refresh Button
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        refreshButton = new JButton("Refresh");
        refreshButton.setToolTipText("Refresh the form.");
        refreshButton.setFont(currentFont);
        refreshButton.addActionListener(e -> refresh());
        add(refreshButton, gbc);
        
        // Load initial categories from the database
        loadCategories();
        
        // Swing Timer to update profit margin in near real time
        new javax.swing.Timer(200, e -> calculateProfitMargin()).start();
        
        // ----------------- Add "Enter" Key Binding for Search -----------------
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "searchItem");
        getActionMap().put("searchItem", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchItem(true);
            }
        });
    }
    
    private void loadCategories() {
        categoryComboBox.removeAllItems();
        try (PreparedStatement stmt = db.getConnection().prepareStatement("SELECT category_name FROM categories");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categoryComboBox.addItem(rs.getString("category_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading categories!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadSubcategories() {
        subcategoryComboBox.removeAllItems();
        String selectedCategory = (String) categoryComboBox.getSelectedItem();
        if (selectedCategory == null) return;
        try (PreparedStatement stmt = db.getConnection().prepareStatement(
                "SELECT subcategory_name FROM subcategories WHERE category_id = (SELECT category_id FROM categories WHERE category_name = ?)")) {
            stmt.setString(1, selectedCategory);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                subcategoryComboBox.addItem(rs.getString("subcategory_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading subcategories!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void calculateProfitMargin() {
        String purchaseText = purchasePriceField.getText().trim();
        String saleText = salePriceField.getText().trim();
        if (purchaseText.isEmpty() || saleText.isEmpty()) {
            profitMarginLabel.setText("Profit Margin: 0%");
            return;
        }
        try {
            double purchasePrice = Double.parseDouble(purchaseText);
            double salePrice = Double.parseDouble(saleText);
            double profitMargin = ((salePrice - purchasePrice) / purchasePrice) * 100;
            profitMarginLabel.setText(String.format("Profit Margin: %.2f%%", profitMargin));
        } catch (NumberFormatException ex) {
            profitMarginLabel.setText("Profit Margin: 0%");
        }
    }
    
    private void searchItem(boolean showMessage) {
        String itemName = itemNameField.getText().trim();
        String barcode = barcodeField.getText().trim();
        String condition = (String) conditionComboBox.getSelectedItem();
        if (itemName.isEmpty() && barcode.isEmpty()) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this, "Enter a Barcode or Item Name to search.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        String query = "";
        PreparedStatement stmt = null;
        Connection conn = db.getConnection();
        try {
            if (!barcode.isEmpty() && !itemName.isEmpty()) {
                query = "SELECT * FROM inventory WHERE item_name = ? AND barcode = ? AND item_condition = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, itemName);
                stmt.setString(2, barcode);
                stmt.setString(3, condition);
            } else if (!barcode.isEmpty()) {
                query = "SELECT * FROM inventory WHERE barcode = ? AND item_condition = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, barcode);
                stmt.setString(2, condition);
            } else {
                query = "SELECT * FROM inventory WHERE item_name = ? AND item_condition = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, itemName);
                stmt.setString(2, condition);
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                barcodeField.setText(rs.getString("barcode"));
                itemNameField.setText(rs.getString("item_name"));
                companyField.setText(rs.getString("company")); // Set company from DB
                categoryComboBox.setSelectedItem(rs.getString("category"));
                subcategoryComboBox.setSelectedItem(rs.getString("subcategory"));
                stockField.setText("0");
                purchasePriceField.setText(String.valueOf(rs.getDouble("purchase_price")));
                salePriceField.setText(String.valueOf(rs.getDouble("sale_price")));
                unitField.setText(rs.getString("unit"));
                profitMarginLabel.setText(String.format("Profit Margin: %.2f%%", rs.getDouble("profit_margin")));
                previewArea.setText(String.format(
                    "Item Name: %s\nBarcode: %s\nCompany: %s\nCategory: %s\nSubcategory: %s\nCondition: %s\nStock: %s\nPurchase Price: %.2f\nSale Price: %.2f\nUnit: %s",
                    rs.getString("item_name"), rs.getString("barcode"), rs.getString("company"),
                    rs.getString("category"), rs.getString("subcategory"),
                    rs.getString("item_condition"), "0", rs.getDouble("purchase_price"), rs.getDouble("sale_price"), rs.getString("unit")));
            } else {
                clearFields();
                previewArea.setText("No matching item found.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching item!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearFields() {
        barcodeField.setText("");
        itemNameField.setText("");
        companyField.setText(""); // Clear company field
        categoryComboBox.setSelectedIndex(-1);
        subcategoryComboBox.removeAllItems();
        stockField.setText("");
        purchasePriceField.setText("");
        salePriceField.setText("");
        unitField.setText("");
        previewArea.setText("");
        profitMarginLabel.setText("Profit Margin: 0%");
    }
    
    private void addCategory() {
        String category = JOptionPane.showInputDialog(this, "Enter New Category Name:");
        if (category == null || category.trim().isEmpty()) return;
        try (PreparedStatement stmt = db.getConnection().prepareStatement("INSERT INTO categories (category_name) VALUES (?)")) {
            stmt.setString(1, category.trim());
            stmt.executeUpdate();
            loadCategories();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding category!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addSubcategory() {
        String category = (String) categoryComboBox.getSelectedItem();
        if (category == null) {
            JOptionPane.showMessageDialog(this, "Select a category first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String subcategory = JOptionPane.showInputDialog(this, "Enter New Subcategory Name:");
        if (subcategory == null || subcategory.trim().isEmpty()) return;
        try (PreparedStatement stmt = db.getConnection().prepareStatement(
                "INSERT INTO subcategories (subcategory_name, category_id) VALUES (?, (SELECT category_id FROM categories WHERE category_name = ?))")) {
            stmt.setString(1, subcategory.trim());
            stmt.setString(2, category);
            stmt.executeUpdate();
            loadSubcategories();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding subcategory!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveItem() {
        String itemName = itemNameField.getText().trim();
        String barcode = barcodeField.getText().trim();
        String company = companyField.getText().trim(); // Get company value
        String category = (String) categoryComboBox.getSelectedItem();
        String subcategory = (String) subcategoryComboBox.getSelectedItem();
        String condition = (String) conditionComboBox.getSelectedItem();
        String unit = unitField.getText().trim();
        int stock = Integer.parseInt(stockField.getText().trim());
        double purchasePrice = Double.parseDouble(purchasePriceField.getText().trim());
        double salePrice = Double.parseDouble(salePriceField.getText().trim());
        
        double computedProfitMargin = ((salePrice - purchasePrice) / purchasePrice) * 100;
        
        try (Connection conn = db.getConnection()) {
            String checkQuery;
            PreparedStatement checkStmt;
            if (barcode.isEmpty()) {
                checkQuery = "SELECT item_id, stock FROM inventory WHERE item_name = ? AND item_condition = ? AND barcode IS NULL";
                checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, itemName);
                checkStmt.setString(2, condition);
            } else {
                checkQuery = "SELECT item_id, stock FROM inventory WHERE (item_name = ? OR barcode = ?) AND item_condition = ?";
                checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, itemName);
                checkStmt.setString(2, barcode);
                checkStmt.setString(3, condition);
            }
            
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                int existingStock = rs.getInt("stock");
                int newStock = existingStock + stock;
                String updateQuery = "UPDATE inventory SET barcode = ?, stock = ?, purchase_price = ?, sale_price = ?, unit = ?, profit_margin = ?, company = ? WHERE item_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    if (barcode.isEmpty()) {
                        updateStmt.setNull(1, Types.VARCHAR);
                    } else {
                        updateStmt.setString(1, barcode);
                    }
                    updateStmt.setInt(2, newStock);
                    updateStmt.setDouble(3, purchasePrice);
                    updateStmt.setDouble(4, salePrice);
                    updateStmt.setString(5, unit);
                    updateStmt.setDouble(6, computedProfitMargin);
                    updateStmt.setString(7, company);
                    updateStmt.setInt(8, rs.getInt("item_id"));
                    updateStmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Item updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String insertQuery = "INSERT INTO inventory (item_name, barcode, category, subcategory, item_condition, stock, purchase_price, sale_price, unit, profit_margin, company) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, itemName);
                    if (barcode.isEmpty()) {
                        insertStmt.setNull(2, Types.VARCHAR);
                    } else {
                        insertStmt.setString(2, barcode);
                    }
                    insertStmt.setString(3, category);
                    insertStmt.setString(4, subcategory);
                    insertStmt.setString(5, condition);
                    insertStmt.setInt(6, stock);
                    insertStmt.setDouble(7, purchasePrice);
                    insertStmt.setDouble(8, salePrice);
                    insertStmt.setString(9, unit);
                    insertStmt.setDouble(10, computedProfitMargin);
                    insertStmt.setString(11, company);
                    insertStmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Item saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            refresh();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving item!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void refresh() {
        loadCategories();
        loadSubcategories();
        barcodeField.setText("");
        itemNameField.setText("");
        companyField.setText(""); // Clear company field
        stockField.setText("");
        purchasePriceField.setText("");
        salePriceField.setText("");
        unitField.setText("");
        previewArea.setText("");
        profitMarginLabel.setText("Profit Margin: 0%");
    }
    
    // ----------------- Language Change Implementation -----------------
    
    public void applyLanguage() {
        if (currentLanguage.equalsIgnoreCase("Urdu")) {
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            
            labelBarcode.setText("بارکوڈ:");
            labelBarcode.setFont(urduFont);
            labelItemName.setText("آئٹم کا نام:");
            labelItemName.setFont(urduFont);
            labelCompany.setText("کمپنی:"); // Urdu text for company
            labelCompany.setFont(urduFont);
            labelCondition.setText("حالت:");
            labelCondition.setFont(urduFont);
            labelCategory.setText("قسم:");
            labelCategory.setFont(urduFont);
            labelSubcategory.setText("ذیلی قسم:");
            labelSubcategory.setFont(urduFont);
            labelStock.setText("اسٹاک:");
            labelStock.setFont(urduFont);
            labelPurchasePrice.setText("خریداری قیمت:");
            labelPurchasePrice.setFont(urduFont);
            labelSalePrice.setText("فروخت قیمت:");
            labelSalePrice.setFont(urduFont);
            labelUnit.setText("یونٹ (اختیاری):");
            labelUnit.setFont(urduFont);
            
            searchButton.setText("آئٹم تلاش کریں");
            searchButton.setFont(urduFont);
            addCategoryButton.setText("قسم شامل کریں");
            addCategoryButton.setFont(urduFont);
            addSubcategoryButton.setText("ذیلی قسم شامل کریں");
            addSubcategoryButton.setFont(urduFont);
            saveButton.setText("آئٹم محفوظ کریں");
            saveButton.setFont(urduFont);
            clearFieldsButton.setText("فیلڈز صاف کریں");
            clearFieldsButton.setFont(urduFont);
            refreshButton.setText("ریفریش");
            refreshButton.setFont(urduFont);
            profitMarginLabel.setText("منافع کی شرح: 0%");
            profitMarginLabel.setFont(urduFont);
            
            barcodeField.setToolTipText("آئٹم کا بارکوڈ درج کریں۔");
            barcodeField.setFont(urduFont);
            itemNameField.setToolTipText("آئٹم کا نام درج کریں۔");
            itemNameField.setFont(urduFont);
            companyField.setToolTipText("آئٹم کی کمپنی درج کریں۔"); // Tooltip for company
            companyField.setFont(urduFont);
            conditionComboBox.setToolTipText("آئٹم کی حالت منتخب کریں۔");
            conditionComboBox.setFont(urduFont);
            categoryComboBox.setToolTipText("آئٹم کی کیٹگری منتخب کریں۔");
            categoryComboBox.setFont(urduFont);
            subcategoryComboBox.setToolTipText("آئٹم کی ذیلی کیٹگری منتخب کریں۔");
            subcategoryComboBox.setFont(urduFont);
            stockField.setToolTipText("اسٹاک کی مقدار درج کریں۔");
            stockField.setFont(urduFont);
            purchasePriceField.setToolTipText("خریداری کی قیمت درج کریں۔");
            purchasePriceField.setFont(urduFont);
            salePriceField.setToolTipText("فروخت کی قیمت درج کریں۔");
            salePriceField.setFont(urduFont);
            unitField.setToolTipText("ماپ کی اکائی درج کریں (مثلاً کلوگرام، لیٹر)۔");
            unitField.setFont(urduFont);
            previewArea.setToolTipText("آئٹم کی تفصیلات کا پیش نظارہ۔");
            previewArea.setFont(urduFont);
        } else {
            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            
            labelBarcode.setText("Barcode:");
            labelBarcode.setFont(englishFont);
            labelItemName.setText("Item Name:");
            labelItemName.setFont(englishFont);
            labelCompany.setText("Company:"); // English text for company
            labelCompany.setFont(englishFont);
            labelCondition.setText("Condition:");
            labelCondition.setFont(englishFont);
            labelCategory.setText("Category:");
            labelCategory.setFont(englishFont);
            labelSubcategory.setText("Subcategory:");
            labelSubcategory.setFont(englishFont);
            labelStock.setText("Stock:");
            labelStock.setFont(englishFont);
            labelPurchasePrice.setText("Purchase Price:");
            labelPurchasePrice.setFont(englishFont);
            labelSalePrice.setText("Sale Price:");
            labelSalePrice.setFont(englishFont);
            labelUnit.setText("Unit (optional):");
            labelUnit.setFont(englishFont);
            
            searchButton.setText("Search Item");
            searchButton.setFont(englishFont);
            addCategoryButton.setText("Add Category");
            addCategoryButton.setFont(englishFont);
            addSubcategoryButton.setText("Add Subcategory");
            addSubcategoryButton.setFont(englishFont);
            saveButton.setText("Save Item");
            saveButton.setFont(englishFont);
            clearFieldsButton.setText("Clear Fields");
            clearFieldsButton.setFont(englishFont);
            refreshButton.setText("Refresh");
            refreshButton.setFont(englishFont);
            profitMarginLabel.setText("Profit Margin: 0%");
            profitMarginLabel.setFont(englishFont);
            
            barcodeField.setToolTipText("Enter the barcode of the item.");
            barcodeField.setFont(englishFont);
            itemNameField.setToolTipText("Enter the name of the item.");
            itemNameField.setFont(englishFont);
            companyField.setToolTipText("Enter the company of the item."); // Tooltip for company
            companyField.setFont(englishFont);
            conditionComboBox.setToolTipText("Select the item condition.");
            conditionComboBox.setFont(englishFont);
            categoryComboBox.setToolTipText("Select the item category.");
            categoryComboBox.setFont(englishFont);
            subcategoryComboBox.setToolTipText("Select the item subcategory.");
            subcategoryComboBox.setFont(englishFont);
            stockField.setToolTipText("Enter the stock quantity.");
            stockField.setFont(englishFont);
            purchasePriceField.setToolTipText("Enter the purchase price.");
            purchasePriceField.setFont(englishFont);
            salePriceField.setToolTipText("Enter the sale price.");
            salePriceField.setFont(englishFont);
            unitField.setToolTipText("Enter the unit of measurement (e.g., kg, liter).");
            unitField.setFont(englishFont);
            previewArea.setToolTipText("Preview of the item details.");
            previewArea.setFont(englishFont);
        }
        revalidate();
        repaint();
    }
    
    public void updateLanguage(String language) {
        currentLanguage = language;
        applyLanguage();
    }
}
