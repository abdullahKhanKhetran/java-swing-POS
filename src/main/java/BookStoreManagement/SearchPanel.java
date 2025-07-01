package BookStoreManagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class SearchPanel extends JPanel implements LanguageChangeListener {
    private Database db;
    private JTable searchTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> searchCategory, searchOption;
    private JButton searchButton;
    private JPanel topPanel;
    private JPanel filterPanel;
    private JComboBox<String> sortByComboBox;
    private JSpinner fromDateSpinner, toDateSpinner;
    private JTextField minValueField, maxValueField;
    private Map<String, String[]> searchOptionsMap;
    
    // Instance variables for top panel labels
    private JLabel searchLabel;
    private JLabel categoryLabel;
    private JLabel searchByLabel;
    private JLabel sortByLabel;
    private JLabel fromLabel;
    private JLabel toLabel;
    
    // Local variable to store current language (default: English)
    private String currentLanguage = "English";
    
    public SearchPanel(Database db) {
        this.db = db;
        setLayout(new BorderLayout());
        
        // Setup search options for different categories.
        searchOptionsMap = new HashMap<>();
        searchOptionsMap.put("Inventory", new String[]{"company","Item Name", "Category", "Subcategory", "Barcode"});
        searchOptionsMap.put("Customers", new String[]{"Customer Name", "Phone", "Address"});
        searchOptionsMap.put("Suppliers", new String[]{"Supplier Name", "Phone", "Address"});
        searchOptionsMap.put("Sales", new String[]{"Sale ID", "Item Name", "Total Price", "Barcode"});
        searchOptionsMap.put("Purchases", new String[]{"Purchase ID", "Item Name", "Total Price", "Barcode"});
        searchOptionsMap.put("Expenses", new String[]{"Description", "Category"});
        searchOptionsMap.put("Transactions", new String[]{"Transaction ID", "Customer/Supplier Name", "Amount", "Date"});

        // Create the table model and table.
        tableModel = new DefaultTableModel();
        searchTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(searchTable);
        add(scrollPane, BorderLayout.CENTER);

        // Build the top panel with search field and labels.
        topPanel = new JPanel(new FlowLayout());
        // Create labels that will be updated in applyLanguage()
        searchLabel = new JLabel("Search:");
        topPanel.add(searchLabel);
        
        searchField = new JTextField(20);
        topPanel.add(searchField);
        
        categoryLabel = new JLabel("Category:");
        topPanel.add(categoryLabel);
        
        searchCategory = new JComboBox<>(searchOptionsMap.keySet().toArray(new String[0]));
        topPanel.add(searchCategory);
        
        searchByLabel = new JLabel("Search By:");
        topPanel.add(searchByLabel);
        
        searchOption = new JComboBox<>();
        topPanel.add(searchOption);
        
        searchButton = new JButton("Search");
        topPanel.add(searchButton);
        
        add(topPanel, BorderLayout.NORTH);

        // Build the filter panel for sort and date filters.
        filterPanel = new JPanel(new FlowLayout());
        sortByLabel = new JLabel("Sort By:");
        filterPanel.add(sortByLabel);
        
        sortByComboBox = new JComboBox<>();
        filterPanel.add(sortByComboBox);
        
        fromLabel = new JLabel("From:");
        toLabel = new JLabel("To:");
        fromDateSpinner = new JSpinner(new SpinnerDateModel());
        toDateSpinner = new JSpinner(new SpinnerDateModel());
        // (Date filters are added conditionally below)
        
        add(filterPanel, BorderLayout.SOUTH);

        // Attach listeners to update options
        searchCategory.addActionListener(e -> {
            updateSearchOptions();
            updateFilterAndSortOptions((String) searchCategory.getSelectedItem());
        });
        searchButton.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());

        updateSearchOptions();
        updateFilterAndSortOptions((String) searchCategory.getSelectedItem());
    }

    private void updateSearchOptions() {
        searchOption.removeAllItems();
        String category = (String) searchCategory.getSelectedItem();
        if (category != null && searchOptionsMap.containsKey(category)) {
            for (String option : searchOptionsMap.get(category)) {
                searchOption.addItem(option);
            }
        }
    }

    private void updateFilterAndSortOptions(String category) {
        filterPanel.removeAll();
        // Add sort by label and combo box
        filterPanel.add(sortByLabel);
        sortByComboBox.removeAllItems();

        switch (category) {
            case "Inventory":
                sortByComboBox.addItem("Stock");
                sortByComboBox.addItem("Sale Price");
                sortByComboBox.addItem("Purchase Price");
                sortByComboBox.addItem("Sold");
                break;
            case "Customers":
            case "Suppliers":
                sortByComboBox.addItem("Balance");
                break;
            case "Sales":
            case "Purchases":
                sortByComboBox.addItem("Total Price");
                sortByComboBox.addItem(category.equals("Sales") ? "Sale Date" : "Purchase Date");
                sortByComboBox.addItem("Quantity");
                break;
            case "Expenses":
                sortByComboBox.addItem("Amount");
                sortByComboBox.addItem("Expense Date");
                break;
            case "Transactions":
                sortByComboBox.addItem("Amount");
                sortByComboBox.addItem("Date");
                break;
        }
        filterPanel.add(sortByComboBox);

        // For certain categories, add date filters.
        if (category.equals("Sales") || category.equals("Purchases") ||
            category.equals("Expenses") || category.equals("Transactions")) {
            filterPanel.add(fromLabel);
            filterPanel.add(fromDateSpinner);
            filterPanel.add(toLabel);
            filterPanel.add(toDateSpinner);
        }
        filterPanel.revalidate();
        filterPanel.repaint();
    }

    private void performSearch() {
        String category = (String) searchCategory.getSelectedItem();
        String searchBy = (String) searchOption.getSelectedItem();
        String searchText = searchField.getText().trim();

        String query = "";
        if (category.equals("Customers") || category.equals("Suppliers")) {
            query = "SELECT name, phone, address, " +
                    "CASE WHEN balance >= 0 THEN balance ELSE 0 END AS 'Remaining', " +
                    "CASE WHEN balance < 0 THEN -balance ELSE 0 END AS 'Advance' " +
                    "FROM " + category.toLowerCase() + " WHERE " + searchBy.replace(" ", "_") + " LIKE ?";
        }
        else if(category.equals("Transactions") && searchBy.equals("Customer/Supplier Name")) {
            query = "SELECT * FROM transactions WHERE name like ?";
        } else {
            query = "SELECT * FROM " + category.toLowerCase() + " WHERE " + searchBy.replace(" ", "_") + " LIKE ?";
        }

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + searchText + "%");
            ResultSet rs = stmt.executeQuery();
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            Vector<String> columns = new Vector<>();

            for (int i = 1; i <= columnCount; i++) {
                columns.add(getLocalizedColumnName(metaData.getColumnName(i)));
            }
            tableModel.setColumnIdentifiers(columns);

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching data: " + ex.getMessage());
        }
    }
    
    // Helper method to return localized column names based on current language.
    private String getLocalizedColumnName(String orig) {
        if (currentLanguage.equalsIgnoreCase("Urdu")) {
            switch (orig.toLowerCase()) {
                case "item_id": return "آئٹم آئی ڈی";
                case "barcode": return "بارکوڈ";
                case "item_name": return "آئٹم کا نام";
                case "category": return "قسم";
                case "subcategory": return "ذیلی قسم";
                case "item_condition": return "حالت";
                case "stock": return "اسٹاک";
                default: return orig;
            }
        } else {
            return orig;
        }
    }
    
    // ----------------- Language Change Implementation -----------------
    
    public void updateLanguage(String language) {
        currentLanguage = language;
        applyLanguage();
    }
    
    @Override
    public void applyLanguage() {
        if (currentLanguage.equalsIgnoreCase("Urdu")) {
            // Update top panel labels
            searchLabel.setText("تلاش:");
            categoryLabel.setText("قسم:");
            searchByLabel.setText("کی بنیاد پر تلاش:");
            sortByLabel.setText("ترتیب:");
            fromLabel.setText("سے:");
            toLabel.setText("تک:");
            
            // Set fonts to Jameel Noori Nastaleeq
            Font urduFont = new Font("Jameel Noori Nastaleeq", Font.BOLD, 16);
            searchLabel.setFont(urduFont);
            categoryLabel.setFont(urduFont);
            searchByLabel.setFont(urduFont);
            sortByLabel.setFont(urduFont);
            fromLabel.setFont(urduFont);
            toLabel.setFont(urduFont);
            searchField.setFont(urduFont);
            searchCategory.setFont(urduFont);
            searchOption.setFont(urduFont);
            searchButton.setFont(urduFont);
            sortByComboBox.setFont(urduFont);
            fromDateSpinner.setFont(urduFont);
            toDateSpinner.setFont(urduFont);
            if (minValueField != null) minValueField.setFont(urduFont);
            if (maxValueField != null) maxValueField.setFont(urduFont);
            
            // Set orientation for the entire panel and sub-panels
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            topPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            topPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            filterPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            filterPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        } else {
            searchLabel.setText("Search:");
            categoryLabel.setText("Category:");
            searchByLabel.setText("Search By:");
            sortByLabel.setText("Sort By:");
            fromLabel.setText("From:");
            toLabel.setText("To:");
            
            Font englishFont = new Font("Arial", Font.BOLD, 16);
            searchLabel.setFont(englishFont);
            categoryLabel.setFont(englishFont);
            searchByLabel.setFont(englishFont);
            sortByLabel.setFont(englishFont);
            fromLabel.setFont(englishFont);
            toLabel.setFont(englishFont);
            searchField.setFont(englishFont);
            searchCategory.setFont(englishFont);
            searchOption.setFont(englishFont);
            searchButton.setFont(englishFont);
            sortByComboBox.setFont(englishFont);
            fromDateSpinner.setFont(englishFont);
            toDateSpinner.setFont(englishFont);
            if (minValueField != null) minValueField.setFont(englishFont);
            if (maxValueField != null) maxValueField.setFont(englishFont);
            
            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            topPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            topPanel.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            filterPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            filterPanel.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        }
        
        // Refresh filter panel so its labels update correctly
        updateFilterAndSortOptions((String) searchCategory.getSelectedItem());
        
        revalidate();
        repaint();
    }
}
