package BookStoreManagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.sql.*;
import java.util.Vector;

public class SupplierDealsDialog extends JDialog {
    private JTable dealsTable;
    private DefaultTableModel dealsModel;
    private JScrollPane scrollPane;
    private int supplierId;
    private Database db;
    private boolean isLoading = false;
    private ResultSet rs; // Scrollable result set
    private int pageSize = 50; // Number of rows to load at a time

    public SupplierDealsDialog(JFrame parent, int supplierId, Database db) {
        super(parent, true);
        this.supplierId = supplierId;
        this.db = db;
        setSize(800, 600);
        setLocationRelativeTo(parent);

        setTitle(fetchSupplierTitle());

        dealsModel = new DefaultTableModel();
        dealsTable = new JTable(dealsModel);
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

        // Load initial data
        initResultSet();
        loadMoreData();
    }

    private String fetchSupplierTitle() {
        String query = "SELECT supplier_name, phone FROM suppliers WHERE supplier_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, supplierId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("supplier_name") + " - " + rs.getString("phone");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching supplier details: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return "Supplier Deals";
    }

    private void initResultSet() {
        String query = "SELECT deal_id, purchase_id, item_name, quantity, Credit , payment_type, deal_date, reversed " +
                       "FROM deals WHERE supplier_id = ? AND purchase_id > 0";  // Excludes reversed purchases

        try {
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setInt(1, supplierId);
            rs = stmt.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error initializing result set: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMoreData() {
        isLoading = true;

        try {
            if (rs == null) return;

            // Set column headers if first load
            if (dealsModel.getColumnCount() == 0) {
                dealsModel.setColumnIdentifiers(new String[]{"Deal ID", "Purchase ID", "Item Name", "Quantity", "Price", "payment", "Deal Date", "Reversed?"});
            }

            int count = 0;
            while (rs.next() && count < pageSize) {
                Vector<Object> rowData = new Vector<>();
                rowData.add(rs.getInt("deal_id"));
                rowData.add(rs.getInt("purchase_id"));
                rowData.add(rs.getString("item_name"));
                rowData.add(rs.getInt("quantity"));
                rowData.add(rs.getDouble("Credit"));
                rowData.add(rs.getString("payment_type"));
                rowData.add(rs.getDate("deal_date"));
                rowData.add(rs.getBoolean("reversed") ? "Yes" : "No"); // Convert boolean to Yes/No

                dealsModel.addRow(rowData);
                count++;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading supplier deals: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            isLoading = false;
        }
    }
}
