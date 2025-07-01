package BookStoreManagement;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

// MainMenu now supports real-time language updates
public class MainMenu extends JPanel {
    private JPanel buttonPanel;
    private JScrollPane menuScrollPane;
    private JPanel screenPanel;
    private CardLayout cardLayout;
    private Database db;
    private String currentLanguage = "Urdu"; // Default language

    // Buttons
    private JButton salesRecordButton;
    private JButton purchaseRecordButton;
    private JButton manageCustomersButton;
    private JButton manageSuppliersButton;
    private JButton profitAndLossButton;
    private JButton reportsButton;
    private JButton searchButton;
    private JButton expenseRecordButton;
    private JButton transactionsButton; // New Transactions button
    private JButton viewInventoryButton;
    private JButton addItemButton;
    private JButton exitButton;

    // Panels
    private AddItemPanel addItemPanel;
    private ViewInventoryPanel viewInventoryPanel;
    // New TransactionsPanel (assumes a constructor taking MainFrame and Database)
    private TransactionsPanel transactionsPanel;

    public MainMenu(MainFrame mainFrame, Database db) {
        this.db = db;
        setLayout(new BorderLayout());

        // Left Scrollable Button Panel
        buttonPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        buttonPanel.setBackground(new Color(50, 50, 50));

        // Create buttons using createStyledButton() (defaulting to Urdu)
        salesRecordButton = createStyledButton("");
        purchaseRecordButton = createStyledButton("");
        manageCustomersButton = createStyledButton("");
        manageSuppliersButton = createStyledButton("");
        profitAndLossButton = createStyledButton("");
        reportsButton = createStyledButton("");
        searchButton = createStyledButton("");
        expenseRecordButton = createStyledButton("");
        transactionsButton = createStyledButton(""); // New button
        viewInventoryButton = createStyledButton("");
        addItemButton = createStyledButton("");
        exitButton = createStyledButton("");
        exitButton.setBackground(Color.RED);

        // Add buttons to panel in the desired order.
        buttonPanel.add(salesRecordButton);
        buttonPanel.add(purchaseRecordButton);
        buttonPanel.add(manageCustomersButton);
        buttonPanel.add(manageSuppliersButton);
        buttonPanel.add(profitAndLossButton);
        buttonPanel.add(reportsButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(expenseRecordButton);
        buttonPanel.add(transactionsButton); // Transactions button inserted here
        buttonPanel.add(viewInventoryButton);
        buttonPanel.add(addItemButton);
        buttonPanel.add(exitButton);

        // Scroll Pane
        menuScrollPane = new JScrollPane(buttonPanel);
        menuScrollPane.setPreferredSize(new Dimension(180, 0));
        menuScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Screen Panel with CardLayout
        cardLayout = new CardLayout();
        screenPanel = new JPanel(cardLayout);

        // Initialize Panels
        addItemPanel = new AddItemPanel(db);
        viewInventoryPanel = new ViewInventoryPanel(db);
        // Create TransactionsPanel by passing the main frame and database
        transactionsPanel = new TransactionsPanel(mainFrame, db);

        // Add Panels to screenPanel
        screenPanel.add(addItemPanel, "AddItem");
        screenPanel.add(viewInventoryPanel, "ViewInventory");
        screenPanel.add(new ManageCustomersPanel(db), "ManageCustomers");
        screenPanel.add(new SearchPanel(db), "Search");
        screenPanel.add(new SalesRecordPanel(db), "SalesRecord");
        screenPanel.add(new ExpenseRecordPanel(db), "ExpenseRecord");
        screenPanel.add(new PurchaseRecordPanel(db), "PurchaseRecord");
        screenPanel.add(new ProfitLossPanel(db), "ProfitAndLoss");
        screenPanel.add(new ManageSupplierPanel(db), "ManageSuppliers");
        screenPanel.add(new ReportsPanel(db), "Reports");
        screenPanel.add(transactionsPanel, "Transactions"); // Add the transactions panel

        // Button Actions
        salesRecordButton.addActionListener(e -> cardLayout.show(screenPanel, "SalesRecord"));
        purchaseRecordButton.addActionListener(e -> cardLayout.show(screenPanel, "PurchaseRecord"));
        manageCustomersButton.addActionListener(e -> cardLayout.show(screenPanel, "ManageCustomers"));
        manageSuppliersButton.addActionListener(e -> cardLayout.show(screenPanel, "ManageSuppliers"));
        profitAndLossButton.addActionListener(e -> cardLayout.show(screenPanel, "ProfitAndLoss"));
        reportsButton.addActionListener(e -> cardLayout.show(screenPanel, "Reports"));
        searchButton.addActionListener(e -> cardLayout.show(screenPanel, "Search"));
        expenseRecordButton.addActionListener(e -> cardLayout.show(screenPanel, "ExpenseRecord"));
        transactionsButton.addActionListener(e -> cardLayout.show(screenPanel, "Transactions")); // Action for transactions button
        viewInventoryButton.addActionListener(e -> {
            viewInventoryPanel.loadInventoryData();
            cardLayout.show(screenPanel, "ViewInventory");
        });
        addItemButton.addActionListener(e -> cardLayout.show(screenPanel, "AddItem"));
        exitButton.addActionListener(e -> mainFrame.showScreen("LoginPage"));

        // Add components to MainMenu
        add(menuScrollPane, BorderLayout.WEST);
        add(screenPanel, BorderLayout.CENTER);

        // Apply default language settings
        applyLanguage();
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(150, 40));
        // Default uses Jameel Noori Nastaleeq. applyLanguage() will update based on language.
        button.setFont(new Font("Jameel Noori Nastaleeq", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 160, 220));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180));
            }
        });
        return button;
    }

    // Update language for main menu buttons directly using setFont.
    public void setLanguage(String language) {
        this.currentLanguage = language;
        applyLanguage();
    }

    public void applyLanguage() {
        if (currentLanguage.equalsIgnoreCase("Urdu")) {
            salesRecordButton.setText("سیلز ریکارڈ");
            purchaseRecordButton.setText("خریداری ریکارڈ");
            manageCustomersButton.setText("گاہکوں کا انتظام");
            manageSuppliersButton.setText("سپلائرز کا انتظام");
            profitAndLossButton.setText("منافع اور نقصان");
            reportsButton.setText("رپورٹس");
            searchButton.setText("تلاش کریں");
            expenseRecordButton.setText("اخراجات کا ریکارڈ");
            transactionsButton.setText("ٹرانزیکشنز"); // Set transactions button text
            viewInventoryButton.setText("اسٹاک دیکھیں");
            addItemButton.setText("آئٹم شامل کریں");
            exitButton.setText("خروج");

            // Set fonts to Jameel Noori Nastaleeq.
            Font urduBtnFont = new Font("Jameel Noori Nastaleeq", Font.BOLD, 16);
            salesRecordButton.setFont(urduBtnFont);
            purchaseRecordButton.setFont(urduBtnFont);
            manageCustomersButton.setFont(urduBtnFont);
            manageSuppliersButton.setFont(urduBtnFont);
            profitAndLossButton.setFont(urduBtnFont);
            reportsButton.setFont(urduBtnFont);
            searchButton.setFont(urduBtnFont);
            expenseRecordButton.setFont(urduBtnFont);
            transactionsButton.setFont(urduBtnFont);
            viewInventoryButton.setFont(urduBtnFont);
            addItemButton.setFont(urduBtnFont);
            exitButton.setFont(urduBtnFont);

            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        } else {
            salesRecordButton.setText("Sales Record");
            purchaseRecordButton.setText("Purchase Record");
            manageCustomersButton.setText("Manage Customers");
            manageSuppliersButton.setText("Manage Suppliers");
            profitAndLossButton.setText("Profit & Loss");
            reportsButton.setText("Reports");
            searchButton.setText("Search");
            expenseRecordButton.setText("Expense Record");
            transactionsButton.setText("Transactions"); // Set transactions button text
            viewInventoryButton.setText("View Inventory");
            addItemButton.setText("Add Item");
            exitButton.setText("Exit");

            // Set fonts to Arial.
            Font englishBtnFont = new Font("Arial", Font.BOLD, 16);
            salesRecordButton.setFont(englishBtnFont);
            purchaseRecordButton.setFont(englishBtnFont);
            manageCustomersButton.setFont(englishBtnFont);
            manageSuppliersButton.setFont(englishBtnFont);
            profitAndLossButton.setFont(englishBtnFont);
            reportsButton.setFont(englishBtnFont);
            searchButton.setFont(englishBtnFont);
            expenseRecordButton.setFont(englishBtnFont);
            transactionsButton.setFont(englishBtnFont);
            viewInventoryButton.setFont(englishBtnFont);
            addItemButton.setFont(englishBtnFont);
            exitButton.setFont(englishBtnFont);

            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        }
        
        // Propagate language update to child panels if they implement updateLanguage(String)
        for (Component comp : screenPanel.getComponents()) {
            try {
                comp.getClass().getMethod("updateLanguage", String.class)
                    .invoke(comp, currentLanguage);
            } catch (Exception e) {
                // Child panel does not implement updateLanguage; ignore.
            }
        }
        
        revalidate();
        repaint();
    }
}
