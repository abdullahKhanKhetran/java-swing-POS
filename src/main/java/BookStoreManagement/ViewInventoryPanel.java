package BookStoreManagement;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.Desktop;
import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;


public class ViewInventoryPanel extends JPanel implements LanguageChangeListener {
    private Database db;
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton, saveButton, deleteButton, exportButton, detailsButton;
    private JComboBox<String> categoryFilter, subcategoryFilter, conditionFilter, stockFilterOperator, companyFilter;
    private JTextField stockFilterValue;
    
    // Top panel and filter panel stored as instance variables for language updates
    private JPanel topPanel;
    private JPanel filterPanel;
    
    // Top panel labels for language updates
    private JLabel categoryLabel;
    private JLabel subcategoryLabel;
    private JLabel conditionLabel;
    private JLabel stockLabel;
    private JLabel companyLabel; // Label for company filter
    
    // Font constants for Urdu and English
    private final Font urduFont = new Font("Jameel Noori Nastaleeq", Font.BOLD, 16);
    private final Font englishFont = new Font("Arial", Font.BOLD, 16);
    
    // Helper method to return the current font based on language
    private Font getCurrentFont() {
        return db.getLanguage().equalsIgnoreCase("Urdu") ? urduFont : englishFont;
    }
    
    public ViewInventoryPanel(Database db) {
        this.db = db;
        setLayout(new BorderLayout());

        // Table setup with scroll pane and custom row highlighting
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make the first column (item_id) uneditable
                return column != 0;
            }
        };
        inventoryTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                // Get stock and minimum_stock values from the model
           int stockCol = ((DefaultTableModel)getModel()).findColumn("stock");
           int minStockCol = ((DefaultTableModel)getModel()).findColumn("minimum_stock");
                try {
                    int stock = Integer.parseInt(String.valueOf(getModel().getValueAt(convertRowIndexToModel(row), stockCol)));
                    int minStock = Integer.parseInt(String.valueOf(getModel().getValueAt(convertRowIndexToModel(row), minStockCol)));
                    if (stock < minStock) {
                        comp.setBackground(new Color(255, 204, 204)); // Light red/pink background
                    } else {
                        comp.setBackground(Color.WHITE);
                    }
                } catch (Exception ex) {
                    comp.setBackground(Color.WHITE);
                }
                return comp;
            }
        };
        inventoryTable.getTableHeader().setFont(getCurrentFont().deriveFont(getCurrentFont().getSize2D() * 4/3f));
        JScrollPane scrollPane = new JScrollPane(inventoryTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        // Top panel for filters and buttons
        topPanel = new JPanel(new FlowLayout());
        
        // Category Label and Filter
        categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(getCurrentFont());
        topPanel.add(categoryLabel);
        
        categoryFilter = new JComboBox<>();
        categoryFilter.setFont(getCurrentFont());
        topPanel.add(categoryFilter);
        
        // Subcategory Label and Filter
        subcategoryLabel = new JLabel("Subcategory:");
        subcategoryLabel.setFont(getCurrentFont());
        topPanel.add(subcategoryLabel);
        
        subcategoryFilter = new JComboBox<>();
        subcategoryFilter.setFont(getCurrentFont());
        topPanel.add(subcategoryFilter);
        
        // Condition Label and Filter
        conditionLabel = new JLabel("Condition:");
        conditionLabel.setFont(getCurrentFont());
        topPanel.add(conditionLabel);
        
        conditionFilter = new JComboBox<>(new String[]{"All", "New", "Like New", "Slightly Used", "Used"});
        conditionFilter.setFont(getCurrentFont());
        topPanel.add(conditionFilter);
        
        // Stock Label, Operator, and Value
        stockLabel = new JLabel("Stock:");
        stockLabel.setFont(getCurrentFont());
        topPanel.add(stockLabel);
        
        stockFilterOperator = new JComboBox<>(new String[]{"Equals to", "Less than", "Greater than"});
        stockFilterOperator.setFont(getCurrentFont());
        topPanel.add(stockFilterOperator);
        
        stockFilterValue = new JTextField(10);
        stockFilterValue.setFont(getCurrentFont());
        topPanel.add(stockFilterValue);
        
        // Company Label and Filter
        companyLabel = new JLabel("Company:");
        companyLabel.setFont(getCurrentFont());
        topPanel.add(companyLabel);
        
        companyFilter = new JComboBox<>();
        companyFilter.setFont(getCurrentFont());
        topPanel.add(companyFilter);
        
        // Buttons
        refreshButton = new JButton("Refresh");
        refreshButton.setFont(getCurrentFont());
        topPanel.add(refreshButton);
        
        saveButton = new JButton("Save");
        saveButton.setFont(getCurrentFont());
        topPanel.add(saveButton);
        
        deleteButton = new JButton("Delete Item");
        deleteButton.setFont(getCurrentFont());
        topPanel.add(deleteButton);
        
        exportButton = new JButton("Export Data");
        exportButton.setFont(getCurrentFont());
        topPanel.add(exportButton);
        
        detailsButton = new JButton("Details");
        detailsButton.setFont(getCurrentFont());
        topPanel.add(detailsButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Filter panel at the bottom (if needed)
        filterPanel = new JPanel(new FlowLayout());
        add(filterPanel, BorderLayout.SOUTH);

        // Event Listeners
        refreshButton.addActionListener(e -> loadInventoryData());
        saveButton.addActionListener(e -> saveChanges());
        deleteButton.addActionListener(e -> deleteItem());
        exportButton.addActionListener(e -> exportData());
        detailsButton.addActionListener(e -> showItemDetails());
        categoryFilter.addActionListener(e -> loadSubcategories());
        conditionFilter.addActionListener(e -> loadInventoryData());
        companyFilter.addActionListener(e -> loadInventoryData());

        // Load initial data
        loadCategories();
        loadCompanyFilter(); // Populate company filter combo box
        loadInventoryData();
    }

    private void loadCategories() {
        categoryFilter.removeAllItems();
        categoryFilter.addItem("All");
        try (PreparedStatement stmt = db.getConnection().prepareStatement("SELECT category_name FROM categories");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categoryFilter.addItem(rs.getString("category_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading categories!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Method to load subcategories based on the selected category
    private void loadSubcategories() {
        subcategoryFilter.removeAllItems();
        subcategoryFilter.addItem("All");
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        if (selectedCategory == null || selectedCategory.equals("All")) return;
        try (PreparedStatement stmt = db.getConnection().prepareStatement(
                "SELECT subcategory_name FROM subcategories WHERE category_id = (SELECT category_id FROM categories WHERE category_name = ?)")) {
            stmt.setString(1, selectedCategory);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                subcategoryFilter.addItem(rs.getString("subcategory_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading subcategories!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Method to load distinct companies into the companyFilter combo box
    private void loadCompanyFilter() {
        companyFilter.removeAllItems();
        companyFilter.addItem("All");
        try (PreparedStatement stmt = db.getConnection().prepareStatement(
                "SELECT DISTINCT company FROM inventory WHERE company IS NOT NULL AND company <> ''");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                companyFilter.addItem(rs.getString("company"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading companies!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadInventoryData() {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        // Updated query: select company and minimum_stock columns and order rows so that low stock rows are on top
        String query = "SELECT item_id, barcode, item_name, company, category, subcategory, item_condition, stock, minimum_stock FROM inventory WHERE 1=1";
        if (!categoryFilter.getSelectedItem().equals("All")) {
            query += " AND category = '" + categoryFilter.getSelectedItem() + "'";
        }
        if (!subcategoryFilter.getSelectedItem().equals("All")) {
            query += " AND subcategory = '" + subcategoryFilter.getSelectedItem() + "'";
        }
        if (!conditionFilter.getSelectedItem().equals("All")) {
            query += " AND item_condition = '" + conditionFilter.getSelectedItem() + "'";
        }
        if (!stockFilterValue.getText().trim().isEmpty()) {
            String operator = (String) stockFilterOperator.getSelectedItem();
            String value = stockFilterValue.getText().trim();
            switch (operator) {
                case "Equals to":
                    query += " AND stock = " + value;
                    break;
                case "Less than":
                    query += " AND stock < " + value;
                    break;
                case "Greater than":
                    query += " AND stock > " + value;
                    break;
            }
        }
        if (!companyFilter.getSelectedItem().equals("All")) {
            query += " AND company = '" + companyFilter.getSelectedItem() + "'";
        }
        // Order so that rows with stock less than minimum_stock come on top
        query += " ORDER BY (stock < minimum_stock) DESC, item_id";
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            String[] columnNames = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                String orig = metaData.getColumnName(i + 1);
                columnNames[i] = getLocalizedColumnName(orig);
            }
            tableModel.setColumnIdentifiers(columnNames);
            while (rs.next()) {
                Vector<Object> rowData = new Vector<>();
                for (int i = 0; i < columnCount; i++) {
                    rowData.add(rs.getObject(i + 1));
                }
                tableModel.addRow(rowData);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading inventory data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Helper method to return localized column names for Urdu
    private String getLocalizedColumnName(String orig) {
        if (db.getLanguage().equalsIgnoreCase("Urdu")) {
            switch (orig.toLowerCase()) {
                case "item_id":
                    return "آئٹم آئی ڈی";
                case "barcode":
                    return "بارکوڈ";
                case "item_name":
                    return "آئٹم کا نام";
                case "company":
                    return "کمپنی";
                case "category":
                    return "قسم";
                case "subcategory":
                    return "ذیلی قسم";
                case "item_condition":
                    return "حالت";
                case "stock":
                    return "اسٹاک";
                case "minimum_stock":
                    return "کم از کم اسٹاک";
                default:
                    return orig;
            }
        } else {
            return orig;
        }
    }

    private void saveChanges() {
        if (db.getConnection() == null) {
            JOptionPane.showMessageDialog(this, "Database connection is not available!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int itemIdColumn = tableModel.findColumn("item_id");
        int itemNameColumn = tableModel.findColumn("item_name");
        int stockColumn = tableModel.findColumn("stock");
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            int itemId = (int) tableModel.getValueAt(row, itemIdColumn);
            String itemName = (String) tableModel.getValueAt(row, itemNameColumn);
            Object stockValue = tableModel.getValueAt(row, stockColumn);
            if (stockValue == null || !(stockValue instanceof Integer)) {
                JOptionPane.showMessageDialog(this, "Invalid stock value for " + itemName + "!", "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            int stock = (int) stockValue;
            try (Connection conn = db.getConnection()) {
                conn.setAutoCommit(true);
                try (PreparedStatement stmt = conn.prepareStatement("UPDATE inventory SET item_name = ?, stock = ? WHERE item_id = ?")) {
                    stmt.setString(1, itemName);
                    stmt.setInt(2, stock);
                    stmt.setInt(3, itemId);
                    stmt.executeUpdate();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating inventory for " + itemName + "!", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        JOptionPane.showMessageDialog(this, "All changes saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteItem() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an item to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int itemId = (int) tableModel.getValueAt(selectedRow, 0);
        String itemName = (String) tableModel.getValueAt(selectedRow, 2);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + itemName + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (PreparedStatement stmt = db.getConnection().prepareStatement("DELETE FROM inventory WHERE item_id = ?")) {
                stmt.setInt(1, itemId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Item deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadInventoryData();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting item!", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new File("Inventory_Report.pdf"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                document.add(new Paragraph("Inventory Report"));
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = now.format(formatter);
                document.add(new Paragraph("Report generated on: " + formattedDateTime));
                Table table = new Table(tableModel.getColumnCount());
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    table.addHeaderCell(tableModel.getColumnName(i));
                }
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        Object cellValue = tableModel.getValueAt(row, col);
                        String cellText = (cellValue == null ? "N/A" : cellValue.toString());
                        table.addCell(cellText);
                    }
                }
                document.add(table);
                JOptionPane.showMessageDialog(this, "PDF exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(fileToSave);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "PDF exported, but unable to open automatically.", "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error exporting PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showItemDetails() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an item to view details.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int itemId = (int) tableModel.getValueAt(selectedRow, 0);
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM inventory WHERE item_id = ?")) {
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                JDialog detailsDialog = new JDialog();
                detailsDialog.setTitle("Item Details");
                detailsDialog.setSize(400, 400);
                detailsDialog.setLayout(new BorderLayout());
                JTextArea detailsArea = new JTextArea();
                detailsArea.setEditable(false);
                detailsArea.setFont(getCurrentFont());
                detailsArea.append("Item ID: " + rs.getInt("item_id") + "\n");
                detailsArea.append("Barcode: " + rs.getString("barcode") + "\n");
                detailsArea.append("Item Name: " + rs.getString("item_name") + "\n");
                detailsArea.append("Category: " + rs.getString("category") + "\n");
                detailsArea.append("Subcategory: " + rs.getString("subcategory") + "\n");
                detailsArea.append("Condition: " + rs.getString("item_condition") + "\n");
                detailsArea.append("Stock: " + rs.getInt("stock") + "\n");
                double purchasePrice = rs.getDouble("purchase_price");
                if (rs.wasNull()) {
                    detailsArea.append("Purchase Price: N/A\n");
                } else {
                    detailsArea.append("Purchase Price: " + purchasePrice + "\n");
                }
                double salePrice = rs.getDouble("sale_price");
                if (rs.wasNull()) {
                    detailsArea.append("Sale Price: N/A\n");
                } else {
                    detailsArea.append("Sale Price: " + salePrice + "\n");
                }
                String unit = rs.getString("unit");
                if (unit == null) {
                    detailsArea.append("Unit: N/A\n");
                } else {
                    detailsArea.append("Unit: " + unit + "\n");
                }
                Timestamp lastChange = rs.getTimestamp("last_change");
                if (lastChange == null) {
                    detailsArea.append("Last Change: N/A\n");
                } else {
                    detailsArea.append("Last Change: " + lastChange + "\n");
                }
                double profitMargin = rs.getDouble("profit_margin");
                if (rs.wasNull()) {
                    detailsArea.append("Profit Margin: N/A\n");
                } else {
                    detailsArea.append("Profit Margin: " + profitMargin + "\n");
                }
                int sold = rs.getInt("sold");
                if (rs.wasNull()) {
                    detailsArea.append("Sold: N/A\n");
                } else {
                    detailsArea.append("Sold: " + sold + "\n");
                }
                detailsDialog.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
                detailsDialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Item not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving item details!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ----------------- Language Change Implementation -----------------
    
    public void applyLanguage() {
        if (db.getLanguage().equalsIgnoreCase("Urdu")) {
            // Set overall orientation to Right-To-Left and update top/filter panels too
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            if (topPanel != null) {
                topPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            }
            if (filterPanel != null) {
                filterPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            }
            
            categoryLabel.setText("قسم:");
            subcategoryLabel.setText("ذیلی قسم:");
            conditionLabel.setText("حالت:");
            stockLabel.setText("اسٹاک:");
            companyLabel.setText("کمپنی:");
            
            categoryFilter.setToolTipText("آئٹم کی کیٹگری منتخب کریں۔");
            subcategoryFilter.setToolTipText("آئٹم کی ذیلی کیٹگری منتخب کریں۔");
            conditionFilter.setToolTipText("آئٹم کی حالت منتخب کریں۔");
            stockFilterOperator.setToolTipText("اسٹاک کا فلٹر آپریٹر منتخب کریں۔");
            stockFilterValue.setToolTipText("اسٹاک کی مقدار درج کریں۔");
            companyFilter.setToolTipText("کمپنی منتخب کریں۔");
            
            refreshButton.setText("ریفریش");
            saveButton.setText("سیو");
            deleteButton.setText("آئٹم ڈیلیٹ کریں");
            exportButton.setText("ڈیٹا ایکسپورٹ کریں");
            detailsButton.setText("تفصیلات");
            
            // Reload data to update column headers in Urdu
            loadInventoryData();
        } else {
            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            if (topPanel != null) {
                topPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            }
            if (filterPanel != null) {
                filterPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            }
            
            categoryLabel.setText("Category:");
            subcategoryLabel.setText("Subcategory:");
            conditionLabel.setText("Condition:");
            stockLabel.setText("Stock:");
            companyLabel.setText("Company:");
            
            categoryFilter.setToolTipText("Select the item category.");
            subcategoryFilter.setToolTipText("Select the item subcategory.");
            conditionFilter.setToolTipText("Select the item condition.");
            stockFilterOperator.setToolTipText("Select the stock filter operator.");
            stockFilterValue.setToolTipText("Enter the stock quantity.");
            companyFilter.setToolTipText("Select the company.");
            
            refreshButton.setText("Refresh");
            saveButton.setText("Save");
            deleteButton.setText("Delete Item");
            exportButton.setText("Export Data");
            detailsButton.setText("Details");
            
            // Reload data to update column headers in English
            loadInventoryData();
        }
        revalidate();
        repaint();
    }
    
    public void updateLanguage(String language) {
        applyLanguage();
    }
}
