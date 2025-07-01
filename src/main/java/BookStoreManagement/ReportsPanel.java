package BookStoreManagement;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class ReportsPanel extends JPanel {
    private Database db;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> reportType, timeFilter;
    private JButton generateButton, exportPdfButton;

    public ReportsPanel(Database db) {
        this.db = db;
        setLayout(new BorderLayout());

        // Table setup
        tableModel = new DefaultTableModel();
        reportTable = new JTable(tableModel);
        add(new JScrollPane(reportTable), BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel();
        reportType = new JComboBox<>(new String[]{"Sales", "Purchases", "Expenses", "Inventory"});
        timeFilter = new JComboBox<>(new String[]{"Today", "Yesterday", "This Week", "This Month", "All Time"});
        generateButton = new JButton("Generate Report");
        exportPdfButton = new JButton("Export to PDF");

        generateButton.addActionListener(e -> generateReport());
        exportPdfButton.addActionListener(e -> exportToPdf());

        controlPanel.add(reportType);
        controlPanel.add(new JLabel("Time Filter:"));
        controlPanel.add(timeFilter);
        controlPanel.add(generateButton);
        controlPanel.add(exportPdfButton);

        add(controlPanel, BorderLayout.NORTH);
    }

    private void generateReport() {
        String selectedReport = (String) reportType.getSelectedItem();
        String selectedTimeFilter = (String) timeFilter.getSelectedItem();
        String query = buildQuery(selectedReport, selectedTimeFilter);

        tableModel.setRowCount(0); // Clear existing rows
        tableModel.setColumnCount(0); // Clear existing columns

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Set column names
            String[] columnNames = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columnNames[i] = metaData.getColumnName(i + 1);
            }
            tableModel.setColumnIdentifiers(columnNames);

            // Add rows (ignoring reversed entries if the column exists)
            while (rs.next()) {
                try {
                    if (rs.getMetaData().getColumnLabel(1).equalsIgnoreCase("reversed") && rs.getBoolean("reversed")) {
                        continue; // Skip reversed entries
                    }
                } catch (SQLException ignore) {}
                Vector<Object> rowData = new Vector<>();
                for (int i = 0; i < columnCount; i++) {
                    rowData.add(rs.getObject(i + 1));
                }
                tableModel.addRow(rowData);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching report data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Builds a query tailored to the selected report type and time filter.
     * 
     * - For "Sales", uses the sale_date column.
     * - For "Purchases", uses the purchase_date column.
     * - For "Expenses", uses expense_date (adjust if needed).
     * - For "Inventory", no date filter is applied.
     */
    private String buildQuery(String reportType, String timeFilter) {
        String tableName;
        String dateColumn = null; // May be null if not applicable

        // Determine table name and date column based on report type
        if (reportType.equalsIgnoreCase("Sales")) {
            tableName = "sales";
            dateColumn = "sale_date";
        } else if (reportType.equalsIgnoreCase("Purchases")) {
            tableName = "purchases";
            dateColumn = "purchase_date";
        } else if (reportType.equalsIgnoreCase("Expenses")) {
            tableName = "expenses";
            dateColumn = "expense_date"; // Adjust as per your table structure
        } else if (reportType.equalsIgnoreCase("Inventory")) {
            tableName = "inventory";
        } else {
            tableName = reportType.toLowerCase();
            dateColumn = "sale_date"; // default fallback
        }

        // Base query. For non-inventory reports, assume there's a "reversed" flag.
        String baseQuery;
        if (reportType.equalsIgnoreCase("Inventory")) {
            baseQuery = "SELECT * FROM " + tableName;
        } else {
            baseQuery = "SELECT * FROM " + tableName + " WHERE reversed = false";
        }

        // If no date column is applicable or if "All Time" is selected, no additional filtering.
        if (dateColumn == null || timeFilter.equals("All Time")) {
            return baseQuery;
        }

        String whereClause = "";
        switch (timeFilter) {
            case "Today":
                whereClause = " AND DATE(" + dateColumn + ") = CURDATE()";
                break;
            case "Yesterday":
                whereClause = " AND DATE(" + dateColumn + ") = DATE_SUB(CURDATE(), INTERVAL 1 DAY)";
                break;
            case "This Week":
                // YEARWEEK with mode 1 assumes Monday as the first day
                whereClause = " AND YEARWEEK(" + dateColumn + ", 1) = YEARWEEK(CURDATE(), 1)";
                break;
            case "This Month":
                whereClause = " AND MONTH(" + dateColumn + ") = MONTH(CURDATE()) AND YEAR(" + dateColumn + ") = YEAR(CURDATE())";
                break;
            default:
                whereClause = "";
                break;
        }
        return baseQuery + whereClause;
    }

    private void exportToPdf() {
        try {
            // Export PDF as "Report.pdf" in current directory
            String filePath = "Report.pdf";
            PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Report: " + reportType.getSelectedItem()).setBold());
            
            // Create table with same column count as tableModel
            Table table = new Table(tableModel.getColumnCount());
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                table.addHeaderCell(new Cell().add(new Paragraph(tableModel.getColumnName(i))));
            }

            // Add rows to the table with null handling
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Object value = tableModel.getValueAt(row, col);
                    String cellText = (value != null ? value.toString() : "N/A");
                    table.addCell(new Cell().add(new Paragraph(cellText)));
                }
            }

            document.add(table);
            document.close();

            JOptionPane.showMessageDialog(this, "Report exported as " + filePath, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error exporting PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
