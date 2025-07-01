package BookStoreManagement;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ProfitLossPanel extends JPanel implements LanguageChangeListener {
    private Database db;
    private JLabel timeFilterLabel, fromDateLabel, toDateLabel;
    private JLabel salesLabel, purchasesLabel, expensesLabel, netProfitLabel;
    private JComboBox<String> timeFilter;
    private JSpinner fromDateSpinner, toDateSpinner;
    private JButton calculateButton;
    private String currentLanguage = "English";
    
    // Fonts
    private static final Font ENGLISH_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final Font URDU_FONT = new Font("Jameel Noori Nastaleeq", Font.PLAIN, 16);

    public ProfitLossPanel(Database db) {
        this.db = db;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Time Filter Components
        gbc.gridx = 0;
        gbc.gridy = 0;
        timeFilterLabel = new JLabel("Time Filter:");
        add(timeFilterLabel, gbc);
        
        gbc.gridx = 1;
        timeFilter = new JComboBox<>(new String[]{"This Month", "Last Month", "Custom Range"});
        timeFilter.addActionListener(e -> updateDateRange());
        add(timeFilter, gbc);

        // From Date Components
        gbc.gridx = 0;
        gbc.gridy++;
        fromDateLabel = new JLabel("From Date:");
        add(fromDateLabel, gbc);
        
        gbc.gridx = 1;
        fromDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor fromEditor = new JSpinner.DateEditor(fromDateSpinner, "yyyy-MM-dd");
        fromDateSpinner.setEditor(fromEditor);
        add(fromDateSpinner, gbc);

        // To Date Components
        gbc.gridx = 0;
        gbc.gridy++;
        toDateLabel = new JLabel("To Date:");
        add(toDateLabel, gbc);
        
        gbc.gridx = 1;
        toDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor toEditor = new JSpinner.DateEditor(toDateSpinner, "yyyy-MM-dd");
        toDateSpinner.setEditor(toEditor);
        add(toDateSpinner, gbc);

        // Calculate Button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        calculateButton = new JButton("Calculate Profit/Loss");
        calculateButton.addActionListener(e -> calculateProfitLoss());
        add(calculateButton, gbc);

        gbc.gridwidth = 1; // Reset grid width

        // Results Labels
        addResultLabel(gbc, "Total Sales:", "PKR 0.00", ++gbc.gridy, salesLabel = new JLabel());
        addResultLabel(gbc, "Total Purchases:", "PKR 0.00", ++gbc.gridy, purchasesLabel = new JLabel());
        addResultLabel(gbc, "Total Expenses:", "PKR 0.00", ++gbc.gridy, expensesLabel = new JLabel());
        addResultLabel(gbc, "Net Profit/Loss:", "PKR 0.00", ++gbc.gridy, netProfitLabel = new JLabel());

        updateDateRange(); // Set initial date range
        applyLanguage(); // Initialize language
    }

    private void addResultLabel(GridBagConstraints gbc, String labelText, String initialValue, int row, JLabel valueLabel) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel label = new JLabel(labelText);
        add(label, gbc);
        
        gbc.gridx = 1;
        valueLabel.setText(initialValue);
        add(valueLabel, gbc);
    }

  
    public void updateLanguage(String language) {
        currentLanguage = language;
        applyLanguage();
    }

    @Override
    public void applyLanguage() {
        if (currentLanguage.equalsIgnoreCase("Urdu")) {
            // Urdu UI
            timeFilterLabel.setText("وقت کا فلٹر:");
            fromDateLabel.setText("تاریخ سے:");
            toDateLabel.setText("تاریخ تک:");
            calculateButton.setText("منافع/نقصان کا حساب لگائیں");
            
            // Update combo box items
            timeFilter.removeAllItems();
            timeFilter.addItem("اس ماہ");
            timeFilter.addItem("پچھلے ماہ");
            timeFilter.addItem("اپنی تاریخ منتخب کریں");
            
            // Update result labels
            updateResultLabels("کل فروخت:", "کل خریداری:", "کل اخراجات:", "خالص منافع/نقصان:");
            
            applyUrduFontAndLayout();
        } else {
            // English UI
            timeFilterLabel.setText("Time Filter:");
            fromDateLabel.setText("From Date:");
            toDateLabel.setText("To Date:");
            calculateButton.setText("Calculate Profit/Loss");
            
            // Update combo box items
            timeFilter.removeAllItems();
            timeFilter.addItem("This Month");
            timeFilter.addItem("Last Month");
            timeFilter.addItem("Custom Range");
            
            // Update result labels
            updateResultLabels("Total Sales:", "Total Purchases:", "Total Expenses:", "Net Profit/Loss:");
            
            applyEnglishFontAndLayout();
        }
        updateDateRange(); // Refresh dates to match language
        revalidate();
        repaint();
    }

    private void updateResultLabels(String sales, String purchases, String expenses, String profit) {
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                String text = label.getText();
                if (text.startsWith("Total Sales:") || text.startsWith("کل فروخت:")) {
                    label.setText(sales);
                } else if (text.startsWith("Total Purchases:") || text.startsWith("کل خریداری:")) {
                    label.setText(purchases);
                } else if (text.startsWith("Total Expenses:") || text.startsWith("کل اخراجات:")) {
                    label.setText(expenses);
                } else if (text.startsWith("Net Profit/Loss:") || text.startsWith("خالص منافع/نقصان:")) {
                    label.setText(profit);
                }
            }
        }
    }

    private void applyUrduFontAndLayout() {
        Font urduFont = URDU_FONT;
        // Fallback if Urdu font not available
        if (!urduFont.getFamily().equals("Jameel Noori Nastaleeq")) {
            urduFont = new Font("SERIF", Font.PLAIN, 16);
        }

        timeFilterLabel.setFont(urduFont);
        fromDateLabel.setFont(urduFont);
        toDateLabel.setFont(urduFont);
        calculateButton.setFont(urduFont);
        
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                comp.setFont(urduFont);
            }
        }
        
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    }

    private void applyEnglishFontAndLayout() {
        timeFilterLabel.setFont(ENGLISH_FONT);
        fromDateLabel.setFont(ENGLISH_FONT);
        toDateLabel.setFont(ENGLISH_FONT);
        calculateButton.setFont(ENGLISH_FONT);
        
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                comp.setFont(ENGLISH_FONT);
            }
        }
        
        setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
    }

    private void updateDateRange() {
        LocalDate now = LocalDate.now();
        String selected = (String) timeFilter.getSelectedItem();
        
        if (currentLanguage.equalsIgnoreCase("Urdu")) {
            if ("اس ماہ".equals(selected)) {
                setDateRange(now.withDayOfMonth(1), now.withDayOfMonth(now.lengthOfMonth()));
            } else if ("پچھلے ماہ".equals(selected)) {
                LocalDate lastMonth = now.minusMonths(1);
                setDateRange(lastMonth.withDayOfMonth(1), lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()));
            }
        } else {
            if ("This Month".equals(selected)) {
                setDateRange(now.withDayOfMonth(1), now.withDayOfMonth(now.lengthOfMonth()));
            } else if ("Last Month".equals(selected)) {
                LocalDate lastMonth = now.minusMonths(1);
                setDateRange(lastMonth.withDayOfMonth(1), lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()));
            }
        }
    }

    private void setDateRange(LocalDate from, LocalDate to) {
        fromDateSpinner.setValue(Date.from(from.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        toDateSpinner.setValue(Date.from(to.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private void calculateProfitLoss() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Date fromDateUtil = (Date) fromDateSpinner.getValue();
        Date toDateUtil = (Date) toDateSpinner.getValue();
        LocalDate fromDate = fromDateUtil.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate toDate = toDateUtil.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        try (Connection conn = db.getConnection()) {
            double totalSales = getTotal(conn, "sales", "total_price", "sale_date", fromDate, toDate, formatter);
            double totalPurchases = getTotal(conn, "purchases", "total_price", "purchase_date", fromDate, toDate, formatter);
            double totalExpenses = getTotal(conn, "expenses", "amount", "expense_date", fromDate, toDate, formatter);

            double netProfit = totalSales - totalPurchases - totalExpenses;

            salesLabel.setText(String.format("PKR %.2f", totalSales));
            purchasesLabel.setText(String.format("PKR %.2f", totalPurchases));
            expensesLabel.setText(String.format("PKR %.2f", totalExpenses));
            netProfitLabel.setText(String.format("PKR %.2f", netProfit));
        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorMessage(
                currentLanguage.equalsIgnoreCase("Urdu") 
                    ? "منافع/نقصان کا حساب لگانے میں خرابی!" 
                    : "Error calculating profit/loss!"
            );
        }
    }

    private double getTotal(Connection conn, String tableName, String column, String dateColumn, 
                          LocalDate fromDate, LocalDate toDate, DateTimeFormatter formatter) throws SQLException {
        double total = 0;
        String query = "SELECT SUM(" + column + ") FROM " + tableName + 
                      " WHERE " + dateColumn + " BETWEEN ? AND ? AND reversed = FALSE";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, fromDate.format(formatter));
            stmt.setString(2, toDate.format(formatter));
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getObject(1) != null) {
                total = rs.getDouble(1);
            }
        }
        return total;
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, 
            currentLanguage.equalsIgnoreCase("Urdu") ? "خرابی" : "Error", 
            JOptionPane.ERROR_MESSAGE);
    }
}