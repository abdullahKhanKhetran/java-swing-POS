package BookStoreManagement;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InvoiceDialog extends JDialog {
    private Database db;
    private List<Object[]> cart;
    private String paymentType, debtorName, debtorPhone;
    private JTextArea invoiceArea;
    private JButton printButton, recordSaleButton, exportPDFButton, closeButton;
    private double totalPrice = 0.0;

    // Fonts for bilingual support
    private Font englishFont = new Font("Monospaced", Font.PLAIN, 12);
    private Font urduFont = loadUrduFont();

    public InvoiceDialog(Frame parent, Database db, List<Object[]> cart, String paymentType, String debtorName, String debtorPhone) {
        super(parent, db.getLanguage().equalsIgnoreCase("Urdu") ? "سیلز رسید" : "Sale Invoice", true);
        this.db = db;
        this.cart = cart;
        this.paymentType = paymentType.toLowerCase();
        this.debtorName = debtorName;
        this.debtorPhone = debtorPhone;

        setSize(600, 500);
        setLayout(new BorderLayout());

        // Invoice area setup
        invoiceArea = new JTextArea();
        if (db.getLanguage().equalsIgnoreCase("Urdu")) {
            invoiceArea.setFont(urduFont);
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        } else {
            invoiceArea.setFont(englishFont);
            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        }
        invoiceArea.setEditable(false);
        generateInvoiceText();
        add(new JScrollPane(invoiceArea), BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        printButton = new JButton(db.getLanguage().equalsIgnoreCase("Urdu") ? "رسید پرنٹ کریں" : "Print Invoice");
        printButton.addActionListener(e -> printInvoice());
        buttonPanel.add(printButton);

        recordSaleButton = new JButton(db.getLanguage().equalsIgnoreCase("Urdu") ? "سیلز ریکارڈ کریں" : "Record Sale");
        recordSaleButton.addActionListener(e -> recordSale());
        buttonPanel.add(recordSaleButton);

        exportPDFButton = new JButton(db.getLanguage().equalsIgnoreCase("Urdu") ? "PDF ایکسپورٹ کریں" : "Export to PDF");
        exportPDFButton.addActionListener(e -> exportToPDF());
        buttonPanel.add(exportPDFButton);

        closeButton = new JButton(db.getLanguage().equalsIgnoreCase("Urdu") ? "بند کریں" : "Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Generates the invoice text based on the current language.
    private void generateInvoiceText() {
        String lang = db.getLanguage();
        StringBuilder invoiceText = new StringBuilder();
        if (lang.equalsIgnoreCase("Urdu")) {
            invoiceText.append("========================================\n");
            invoiceText.append("              سیلز رسید              \n");
            invoiceText.append("========================================\n");
            if (paymentType.equalsIgnoreCase("account")) {
                invoiceText.append("ادھار لینے والے کا نام   : ").append(debtorName).append("\n");
                invoiceText.append("ادھار لینے والے کا فون   : ").append(debtorPhone).append("\n");
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String saleDate = LocalDateTime.now().format(formatter);
            invoiceText.append("سیلز کی تاریخ     : ").append(saleDate).append("\n");
            invoiceText.append("========================================\n");
            invoiceText.append(String.format("%-15s %-10s %-10s %-10s\n", "آئٹم کا نام", "مقدار", "فروخت قیمت", "کل"));
            invoiceText.append("========================================\n");
        } else {
            invoiceText.append("========================================\n");
            invoiceText.append("              SALE INVOICE              \n");
            invoiceText.append("========================================\n");
            if (paymentType.equalsIgnoreCase("account")) {
                invoiceText.append("Debtor Name   : ").append(debtorName).append("\n");
                invoiceText.append("Debtor Phone  : ").append(debtorPhone).append("\n");
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String saleDate = LocalDateTime.now().format(formatter);
            invoiceText.append("Sale Date     : ").append(saleDate).append("\n");
            invoiceText.append("========================================\n");
            invoiceText.append(String.format("%-15s %-10s %-10s %-10s\n", "Item Name", "Quantity", "Sale Price", "Total"));
            invoiceText.append("========================================\n");
        }

        totalPrice = 0.0;
        for (Object[] item : cart) {
            String itemName = (String) item[0];
            int quantity = (int) item[2];
            double salePrice = (double) item[3];
            double lineTotal = salePrice * quantity;
            invoiceText.append(String.format("%-15s %-10d %-10.2f %-10.2f\n", itemName, quantity, salePrice, lineTotal));
            totalPrice += lineTotal;
        }
        invoiceText.append("========================================\n");
        if (lang.equalsIgnoreCase("Urdu")) {
            invoiceText.append(String.format("کل قیمت   : PKR %.2f\n", totalPrice));
            invoiceText.append("========================================\n");
        } else {
            invoiceText.append(String.format("Total Price   : PKR %.2f\n", totalPrice));
            invoiceText.append("========================================\n");
        }
        invoiceArea.setText(invoiceText.toString());
    }

    private void printInvoice() {
        try {
            invoiceArea.print();
        } catch (PrinterException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while printing: " + e.getMessage(), "Printing Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recordSale() {
        // ... (your recordSale implementation remains unchanged) ...
    }

    private void exportToPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(db.getLanguage().equalsIgnoreCase("Urdu") ? "سیلز رسید محفوظ کریں" : "Save Sale Invoice");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".pdf")) {
                    filePath += ".pdf";
                }
                PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc);

                if (db.getLanguage().equalsIgnoreCase("Urdu")) {
                    document.add(new Paragraph("========================================"));
                    document.add(new Paragraph("              سیلز رسید              ").setBold());
                    document.add(new Paragraph("========================================"));
                    document.add(new Paragraph("ادھار لینے والے کا نام   : " + debtorName));
                    document.add(new Paragraph("ادھار لینے والے کا فون   : " + debtorPhone));
                } else {
                    document.add(new Paragraph("========================================"));
                    document.add(new Paragraph("              SALE INVOICE              ").setBold());
                    document.add(new Paragraph("========================================"));
                    if (paymentType.equalsIgnoreCase("account")) {
                        document.add(new Paragraph("Debtor Name   : " + debtorName));
                        document.add(new Paragraph("Debtor Phone  : " + debtorPhone));
                    }
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String saleDate = LocalDateTime.now().format(formatter);
                if (db.getLanguage().equalsIgnoreCase("Urdu")) {
                    document.add(new Paragraph("سیلز کی تاریخ     : " + saleDate));
                } else {
                    document.add(new Paragraph("Sale Date     : " + saleDate));
                }
                document.add(new Paragraph("========================================"));
                if (db.getLanguage().equalsIgnoreCase("Urdu")) {
                    document.add(new Paragraph(String.format("%-15s %-10s %-10s %-10s", "آئٹم کا نام", "مقدار", "فروخت قیمت", "کل")));
                } else {
                    document.add(new Paragraph(String.format("%-15s %-10s %-10s %-10s", "Item Name", "Quantity", "Sale Price", "Total")));
                }
                document.add(new Paragraph("========================================"));

                for (Object[] item : cart) {
                    String itemName = (String) item[0];
                    int quantity = (int) item[2];
                    double salePrice = (double) item[3];
                    double lineTotal = salePrice * quantity;
                    document.add(new Paragraph(String.format("%-15s %-10d %-10.2f %-10.2f", itemName, quantity, salePrice, lineTotal)));
                }
                document.add(new Paragraph("========================================"));
                if (db.getLanguage().equalsIgnoreCase("Urdu")) {
                    document.add(new Paragraph(String.format("کل قیمت   : PKR %.2f", totalPrice)));
                    document.add(new Paragraph("========================================"));
                } else {
                    document.add(new Paragraph(String.format("Total Price   : PKR %.2f", totalPrice)));
                    document.add(new Paragraph("========================================"));
                }
                document.close();
                JOptionPane.showMessageDialog(this, db.getLanguage().equalsIgnoreCase("Urdu") ? "رسید کامیابی سے محفوظ ہو گئی:\n" + filePath : "Invoice saved successfully at:\n" + filePath, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error exporting to PDF!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static Font loadUrduFont() {
        // Implement your font loading logic here.
        // For example, if you have a TTF file in your resources, load it via:
        // Font.createFont(Font.TRUETYPE_FONT, InvoiceDialog.class.getResourceAsStream("/path/to/JameelNooriNastaleeq.ttf")).deriveFont(12f);
        // For simplicity, we return a fallback font.
        return new Font("Serif", Font.PLAIN, 12);
    }
}
