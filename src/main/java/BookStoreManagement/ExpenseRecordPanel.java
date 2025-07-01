package BookStoreManagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class ExpenseRecordPanel extends JPanel implements LanguageChangeListener {
    private Database db;
    private JTable expensesTable;
    private DefaultTableModel tableModel;
    
    // Input fields and their labels
    private JTextField descriptionField, amountField, newCategoryField;
    private JLabel descriptionLabel, amountLabel, categoryLabel, newCategoryLabel;
    
    // Buttons
    private JButton saveButton, refreshButton, reverseButton, addCategoryButton;
    
    // Filter controls and their labels
    private JSpinner fromDateSpinner, toDateSpinner;
    private JComboBox<String> filterTimeComboBox, reverseComboBox, categoryComboBox;
    private JLabel filterTimeLabel, fromLabel, toLabel;
    
    // Local variable to store current language (default: English)
    private String currentLanguage = "English";
    
    public ExpenseRecordPanel(Database db) {
        this.db = db;
        setLayout(new BorderLayout());

        // Table setup
        tableModel = new DefaultTableModel();
        expensesTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(expensesTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        // Top panel for inputs
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Description input
        gbc.gridx = 0; gbc.gridy = 0;
        descriptionLabel = new JLabel("Description:");
        topPanel.add(descriptionLabel, gbc);
        gbc.gridx = 1;
        descriptionField = new JTextField(15);
        topPanel.add(descriptionField, gbc);

        // Amount input
        gbc.gridx = 0; gbc.gridy++;
        amountLabel = new JLabel("Amount:");
        topPanel.add(amountLabel, gbc);
        gbc.gridx = 1;
        amountField = new JTextField(10);
        topPanel.add(amountField, gbc);

        // Category selection for expense input
        gbc.gridx = 0; gbc.gridy++;
        categoryLabel = new JLabel("Category:");
        topPanel.add(categoryLabel, gbc);
        gbc.gridx = 1;
        categoryComboBox = new JComboBox<>();
        loadCategories();
        topPanel.add(categoryComboBox, gbc);

        // New Category input and Add button
        gbc.gridx = 0; gbc.gridy++;
        newCategoryLabel = new JLabel("New Category:");
        topPanel.add(newCategoryLabel, gbc);
        gbc.gridx = 1;
        newCategoryField = new JTextField(10);
        topPanel.add(newCategoryField, gbc);
        gbc.gridx = 2;
        addCategoryButton = new JButton("Add Category");
        addCategoryButton.addActionListener(e -> addCategory());
        topPanel.add(addCategoryButton, gbc);

        // Save Expense Button
        gbc.gridx = 2; gbc.gridy = 0;
        saveButton = new JButton("Save Expense");
        saveButton.addActionListener(e -> saveExpense());
        topPanel.add(saveButton, gbc);

        // Reverse Expense Button
        gbc.gridx = 2; gbc.gridy++;
        reverseButton = new JButton("Reverse Expense");
        reverseButton.addActionListener(e -> reverseExpense());
        topPanel.add(reverseButton, gbc);

        // Refresh Button
        gbc.gridx = 3; gbc.gridy = 0;
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadExpensesData());
        topPanel.add(refreshButton, gbc);

        add(topPanel, BorderLayout.NORTH);

        // Filter Panel (for time period and reversal filter)
        JPanel bottomPanel = new JPanel(new FlowLayout());
        filterTimeLabel = new JLabel("Time Period:");
        bottomPanel.add(filterTimeLabel);
        filterTimeComboBox = new JComboBox<>(new String[]{"All Time", "Custom"});
        filterTimeComboBox.addActionListener(e -> toggleDateFilters());
        bottomPanel.add(filterTimeComboBox);
        fromLabel = new JLabel("From:");
        bottomPanel.add(fromLabel);
        fromDateSpinner = new JSpinner(new SpinnerDateModel());
        bottomPanel.add(fromDateSpinner);
        toLabel = new JLabel("To:");
        bottomPanel.add(toLabel);
        toDateSpinner = new JSpinner(new SpinnerDateModel());
        bottomPanel.add(toDateSpinner);
        reverseComboBox = new JComboBox<>(new String[]{"ALL", "VALID", "REVERSED"});
        reverseComboBox.addActionListener(e -> loadExpensesData());
        bottomPanel.add(reverseComboBox);

        add(bottomPanel, BorderLayout.SOUTH);

        toggleDateFilters();
        loadExpensesData();
    }

    // Enables/disables the date filters based on selection
    private void toggleDateFilters() {
        boolean isCustom = filterTimeComboBox.getSelectedItem().equals("Custom");
        fromDateSpinner.setEnabled(isCustom);
        toDateSpinner.setEnabled(isCustom);
    }

    // Loads expense categories from the database
    private void loadCategories() {
        categoryComboBox.removeAllItems();
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT category_name FROM expense_categories")) {

            while (rs.next()) {
                categoryComboBox.addItem(rs.getString("category_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading categories: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Adds a new category to the database
    private void addCategory() {
        String newCategory = newCategoryField.getText().trim();
        if (newCategory.isEmpty()) {
            JOptionPane.showMessageDialog(this, currentLanguage.equalsIgnoreCase("Urdu") ?
                    "کیٹیگری خالی نہیں ہو سکتی!" : "Category name cannot be empty!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO expense_categories (category_name) VALUES (?)")) {
            stmt.setString(1, newCategory);
            stmt.executeUpdate();
            loadCategories();
            newCategoryField.setText("");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding category: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Saves a new expense into the database
    private void saveExpense() {
        String description = descriptionField.getText().trim();
        String amountText = amountField.getText().trim();
        String category = (String) categoryComboBox.getSelectedItem();
        if (description.isEmpty() || amountText.isEmpty() || category == null) {
            JOptionPane.showMessageDialog(this, currentLanguage.equalsIgnoreCase("Urdu") ?
                    "براہ مہربانی تمام فیلڈز پُر کریں!" : "Please fill all fields!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO expenses (description, amount, category, expense_date, reversed) VALUES (?, ?, ?, CURRENT_DATE, FALSE)")) {
            stmt.setString(1, description);
            stmt.setDouble(2, Double.parseDouble(amountText));
            stmt.setString(3, category);
            stmt.executeUpdate();
            loadExpensesData();
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving expense: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Loads expense records from the database and updates the table
    public void loadExpensesData() {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        String query = "SELECT expense_id, description, amount, category, expense_date, reversed FROM expenses";
        String filter = (String) reverseComboBox.getSelectedItem();
        if (filter != null && !filter.equalsIgnoreCase("ALL")) {
            query += " WHERE reversed = " + (filter.equalsIgnoreCase("VALID") ? "FALSE" : "TRUE");
        }

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            // Set column headers based on language
            String[] columnNames;
            if (currentLanguage.equalsIgnoreCase("Urdu")) {
                columnNames = new String[]{
                    "اخراجات آئی ڈی", "تفصیل", "رقم", "قسم", "اخراجات کی تاریخ", "ریورس شدہ"
                };
            } else {
                columnNames = new String[]{
                    "Expense ID", "Description", "Amount", "Category", "Expense Date", "Reversed"
                };
            }
            tableModel.setColumnIdentifiers(columnNames);

            while (rs.next()) {
                Vector<Object> rowData = new Vector<>();
                for (int i = 1; i <= columnNames.length - 1; i++) {
                    rowData.add(rs.getObject(i));
                }
                boolean rev = rs.getBoolean("reversed");
                String revText = currentLanguage.equalsIgnoreCase("Urdu") ? (rev ? "ہاں" : "نہیں") : (rev ? "Yes" : "No");
                rowData.add(revText);
                tableModel.addRow(rowData);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading expenses: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Reverses an expense by setting its 'reversed' field to TRUE in the database
    private void reverseExpense() {
        int selectedRow = expensesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, currentLanguage.equalsIgnoreCase("Urdu") ?
                    "براہ کرم ریورس کرنے کے لیے ایک اخراج منتخب کریں!" : "Please select an expense to reverse!",
                    currentLanguage.equalsIgnoreCase("Urdu") ? "خرابی" : "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int expenseId = (int) tableModel.getValueAt(selectedRow, 0);
        String reversedValue = tableModel.getValueAt(selectedRow, 5).toString();
        if (reversedValue.equalsIgnoreCase(currentLanguage.equalsIgnoreCase("Urdu") ? "ہاں" : "Yes")) {
            JOptionPane.showMessageDialog(this, currentLanguage.equalsIgnoreCase("Urdu") ?
                    "یہ اخراج پہلے ہی ریورس ہو چکا ہے!" : "This expense has already been reversed!",
                    currentLanguage.equalsIgnoreCase("Urdu") ? "خرابی" : "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, currentLanguage.equalsIgnoreCase("Urdu") ?
                "کیا آپ واقعی اس اخراج کو ریورس کرنا چاہتے ہیں؟" : "Are you sure you want to reverse this expense?",
                currentLanguage.equalsIgnoreCase("Urdu") ? "تصدیق کریں" : "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE expenses SET reversed = TRUE WHERE expense_id = ?")) {
            stmt.setInt(1, expenseId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, currentLanguage.equalsIgnoreCase("Urdu") ?
                    "اخراج کامیابی سے ریورس ہو گیا!" : "Expense reversed successfully!",
                    currentLanguage.equalsIgnoreCase("Urdu") ? "کامیابی" : "Success", JOptionPane.INFORMATION_MESSAGE);
            loadExpensesData();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, currentLanguage.equalsIgnoreCase("Urdu") ?
                    "اخراج ریورس کرنے میں خرابی!" : "Error reversing expense!",
                    currentLanguage.equalsIgnoreCase("Urdu") ? "ڈیٹا بیس خرابی" : "Database Error", JOptionPane.ERROR_MESSAGE);
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
            // Update top panel labels to Urdu
            descriptionLabel.setText("تفصیل:");
            amountLabel.setText("رقم:");
            categoryLabel.setText("قسم:");
            newCategoryLabel.setText("نئی قسم:");
            saveButton.setText("اخراج محفوظ کریں");
            reverseButton.setText("اخراج ریورس کریں");
            refreshButton.setText("ریفریش");
            filterTimeLabel.setText("وقت کی مدت:");
            fromLabel.setText("سے:");
            toLabel.setText("تک:");
            
            // Set fonts for all components to Jameel Noori Nastaleeq with increased size
            Font urduFont = new Font("Jameel Noori Nastaleeq", Font.BOLD, 18);
            descriptionLabel.setFont(urduFont);
            amountLabel.setFont(urduFont);
            categoryLabel.setFont(urduFont);
            newCategoryLabel.setFont(urduFont);
            saveButton.setFont(urduFont);
            reverseButton.setFont(urduFont);
            refreshButton.setFont(urduFont);
            filterTimeLabel.setFont(urduFont);
            fromLabel.setFont(urduFont);
            toLabel.setFont(urduFont);
            fromDateSpinner.setFont(urduFont);
            toDateSpinner.setFont(urduFont);
            categoryComboBox.setFont(urduFont);
            
            // Update table headers to Urdu
            tableModel.setColumnIdentifiers(new String[]{
                "اخراجات آئی ڈی", "تفصیل", "رقم", "قسم", "اخراجات کی تاریخ", "ریورس شدہ"
            });
            expensesTable.getTableHeader().setFont(urduFont.deriveFont(urduFont.getSize2D() * 4/3f));

            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        } else {
            descriptionLabel.setText("Description:");
            amountLabel.setText("Amount:");
            categoryLabel.setText("Category:");
            newCategoryLabel.setText("New Category:");
            saveButton.setText("Save Expense");
            reverseButton.setText("Reverse Expense");
            refreshButton.setText("Refresh");
            filterTimeLabel.setText("Time Period:");
            fromLabel.setText("From:");
            toLabel.setText("To:");
            
            Font englishFont = new Font("Arial", Font.BOLD, 18);
            descriptionLabel.setFont(englishFont);
            amountLabel.setFont(englishFont);
            categoryLabel.setFont(englishFont);
            newCategoryLabel.setFont(englishFont);
            saveButton.setFont(englishFont);
            reverseButton.setFont(englishFont);
            refreshButton.setFont(englishFont);
            filterTimeLabel.setFont(englishFont);
            fromLabel.setFont(englishFont);
            toLabel.setFont(englishFont);
            fromDateSpinner.setFont(englishFont);
            toDateSpinner.setFont(englishFont);
            categoryComboBox.setFont(englishFont);
            
            tableModel.setColumnIdentifiers(new String[]{
                "Expense ID", "Description", "Amount", "Category", "Expense Date", "Reversed"
            });
            expensesTable.getTableHeader().setFont(englishFont.deriveFont(englishFont.getSize2D() * 4/3f));

            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        }
        revalidate();
        repaint();
    }
}
