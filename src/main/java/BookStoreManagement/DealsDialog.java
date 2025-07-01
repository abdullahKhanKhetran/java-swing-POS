package BookStoreManagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.sql.*;
import java.util.Vector;

public class DealsDialog extends JDialog {
    private JTable dealsTable;
    private LazyLoadingTableModel dealsModel;
    private JScrollPane scrollPane;
    private int id;
    private String type;
    private Database db;
    private int currentOffset = 0;
    private int pageSize = 50; // Number of rows to load at a time
    private boolean isLoading = false; // To prevent multiple simultaneous loads

    // English and Urdu column headers
    private final String[] englishColumns = {"Deal ID", "Sale ID", "Item Name", "Quantity", "Total Price", "Credit", "Deal Date", "Reversed"};
    private final String[] urduColumns = {"ڈیل آئی ڈی", "سیل آئی ڈی", "آئٹم کا نام", "مقدار", "کل قیمت", "کریڈٹ", "ڈیل کی تاریخ", "الٹ دیا گیا"};

    public DealsDialog(JFrame parent, String title, int id, String type, Database db) {
        super(parent, title, true);
        this.id = id;
        this.type = type;
        this.db = db;
        setSize(800, 600);
        setLocationRelativeTo(parent);

        // Table setup with lazy loading model
        dealsModel = new LazyLoadingTableModel();
        dealsTable = new JTable(dealsModel);
        // Increase table header font size by 3 points over base (assume base is 12)
        dealsTable.getTableHeader().setFont(getCurrentFont().deriveFont((float)getCurrentFont().getSize() + 3));
        scrollPane = new JScrollPane(dealsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Add scroll listener for lazy loading
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!e.getValueIsAdjusting() && !isLoading) {
                    JScrollBar scrollBar = (JScrollBar) e.getSource();
                    int extent = scrollBar.getModel().getExtent();
                    int maximum = scrollBar.getModel().getMaximum();
                    int value = scrollBar.getValue();
                    if (value + extent >= maximum) {
                        loadMoreData();
                    }
                }
            }
        });

        // Load initial data and apply bilingual support (one-time setup)
        loadMoreData();
        applyLanguage();
    }

    private void loadMoreData() {
        isLoading = true;
        String query = "SELECT deal_id, sale_id, item_name, quantity, debit AS total_price, credit, deal_date, reversed " +
                       "FROM deals WHERE " + (type.equals("customer") ? "customer_id" : "supplier_id") + " = ? " +
                       "AND sale_id >= 0 LIMIT ? OFFSET ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, currentOffset);
            ResultSet rs = stmt.executeQuery();

            // Set column headers (only for first load)
            if (dealsModel.getColumnCount() == 0) {
                // We'll set the headers via applyLanguage() later.
                dealsModel.setColumnIdentifiers(englishColumns);
            }

            while (rs.next()) {
                Vector<Object> rowData = new Vector<>();
                rowData.add(rs.getInt("deal_id"));
                rowData.add(rs.getInt("sale_id"));
                rowData.add(rs.getString("item_name"));
                rowData.add(rs.getInt("quantity"));
                rowData.add(rs.getDouble("total_price"));
                rowData.add(rs.getDouble("credit"));
                rowData.add(rs.getDate("deal_date"));
                rowData.add(rs.getBoolean("reversed"));
                dealsModel.addRow(rowData);
            }
            currentOffset += pageSize;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching deals: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            isLoading = false;
        }
    }

    // Helper method to return current base font (we assume base size 12)
    private Font getCurrentFont() {
        if (db.getLanguage().equalsIgnoreCase("Urdu")) {
            return new Font("Jameel Noori Nastaleeq", Font.PLAIN, 12);
        } else {
            return new Font("Arial", Font.PLAIN, 12);
        }
    }

    // --------------------- applyLanguage() ---------------------
    public void applyLanguage() {
        // Check current language from the database and adjust UI accordingly.
        if (db.getLanguage().equalsIgnoreCase("Urdu")) {
            // Set orientation to RIGHT_TO_LEFT.
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            // Set table column headers to Urdu.
            dealsModel.setColumnIdentifiers(urduColumns);
            // Increase font size by 3 (base 12 becomes 15).
            Font newUrduFont = new Font("Jameel Noori Nastaleeq", Font.PLAIN, 15);
            dealsTable.setFont(newUrduFont);
            dealsTable.getTableHeader().setFont(newUrduFont);
        } else {
            // Set orientation to LEFT_TO_RIGHT.
            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            dealsModel.setColumnIdentifiers(englishColumns);
            Font newEnglishFont = new Font("Arial", Font.PLAIN, 15);
            dealsTable.setFont(newEnglishFont);
            dealsTable.getTableHeader().setFont(newEnglishFont);
        }
        revalidate();
        repaint();
    }

    // Custom TableModel for lazy loading
    private class LazyLoadingTableModel extends DefaultTableModel {
        public LazyLoadingTableModel() {
            super(new Vector<>(), 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
