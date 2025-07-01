package BookStoreManagement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class NewItemDialog extends JDialog {
    private JTextField categoryField, subcategoryField, salePriceField;
    private JButton addCategoryButton, addSubcategoryButton, okButton, cancelButton;
    private Database db; // Reference for language
    private JPanel inputPanel;
    private JPanel buttonPanel;

    public NewItemDialog(Frame parent, Database db) {
        super(parent, "Add New Inventory Item", true);
        this.db = db;
        setLayout(new BorderLayout());
        
        // Input panel with GridBagLayout
        inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Category label and field
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Category:"), gbc);
        
        categoryField = new JTextField(10);
        gbc.gridx = 1;
        inputPanel.add(categoryField, gbc);
        
        addCategoryButton = new JButton("Add Category");
        gbc.gridx = 2;
        inputPanel.add(addCategoryButton, gbc);
        
        // Subcategory label and field
        gbc.gridx = 3;
        inputPanel.add(new JLabel("Subcategory:"), gbc);
        
        subcategoryField = new JTextField(10);
        gbc.gridx = 4;
        inputPanel.add(subcategoryField, gbc);
        
        addSubcategoryButton = new JButton("Add Subcategory");
        gbc.gridx = 5;
        inputPanel.add(addSubcategoryButton, gbc);
        
        // Sale Price label and field
        gbc.gridx = 6;
        inputPanel.add(new JLabel("Sale Price:"), gbc);
        
        salePriceField = new JTextField(8);
        gbc.gridx = 7;
        inputPanel.add(salePriceField, gbc);
        
        // Button panel with OK and Cancel buttons
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(parent);
        
        // Action Listeners
        addCategoryButton.addActionListener(e -> addCategory());
        addSubcategoryButton.addActionListener(e -> addSubcategory());
        okButton.addActionListener(e -> onOK());
        cancelButton.addActionListener(e -> onCancel());
        
        // Apply bilingual support once (dialog is modal)
        applyLanguage();
    }
    
    // Stub method for adding a category (customize as needed)
    private void addCategory() {
        JOptionPane.showMessageDialog(this, "Category added: " + categoryField.getText().trim());
    }
    
    // Stub method for adding a subcategory (customize as needed)
    private void addSubcategory() {
        JOptionPane.showMessageDialog(this, "Subcategory added: " + subcategoryField.getText().trim());
    }
    
    private void onOK() {
        if (categoryField.getText().trim().isEmpty() || subcategoryField.getText().trim().isEmpty() || salePriceField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Double.parseDouble(salePriceField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Enter a valid sale price.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        setVisible(false);
    }
    
    private void onCancel() {
        setVisible(false);
    }
    
    // Getters for entered details
    public String getCategory() {
        return categoryField.getText().trim();
    }
    
    public String getSubcategory() {
        return subcategoryField.getText().trim();
    }
    
    public double getSalePrice() {
        try {
            return Double.parseDouble(salePriceField.getText().trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    // --------------------- applyLanguage() ---------------------
    // Non-dynamic bilingual support: apply language settings once during construction.
    private void applyLanguage() {
        if (db.getLanguage().equalsIgnoreCase("Urdu")) {
            setTitle("نیا آئٹم شامل کریں");
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            inputPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            buttonPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            // Update labels in inputPanel (fixed order)
            ((JLabel) inputPanel.getComponent(0)).setText("قسم:");
            ((JLabel) inputPanel.getComponent(3)).setText("ذیلی قسم:");
            ((JLabel) inputPanel.getComponent(6)).setText("فروخت کی قیمت:");
            // Update button texts
            addCategoryButton.setText("قسم شامل کریں");
            addSubcategoryButton.setText("ذیلی قسم شامل کریں");
            okButton.setText("ٹھیک ہے");
            cancelButton.setText("منسوخ کریں");
            // Set fonts to Jameel Noori Nastaleeq with increased size (e.g., 16)
            Font urduFont = new Font("Jameel Noori Nastaleeq", Font.PLAIN, 16);
            Component[] comps = inputPanel.getComponents();
            for (Component comp : comps) {
                comp.setFont(urduFont);
            }
            for (Component comp : buttonPanel.getComponents()) {
                comp.setFont(urduFont);
            }
        } else {
            setTitle("Add New Inventory Item");
            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            inputPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            buttonPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            ((JLabel) inputPanel.getComponent(0)).setText("Category:");
            ((JLabel) inputPanel.getComponent(3)).setText("Subcategory:");
            ((JLabel) inputPanel.getComponent(6)).setText("Sale Price:");
            addCategoryButton.setText("Add Category");
            addSubcategoryButton.setText("Add Subcategory");
            okButton.setText("OK");
            cancelButton.setText("Cancel");
            Font englishFont = new Font("Arial", Font.PLAIN, 16);
            Component[] comps = inputPanel.getComponents();
            for (Component comp : comps) {
                comp.setFont(englishFont);
            }
            for (Component comp : buttonPanel.getComponents()) {
                comp.setFont(englishFont);
            }
        }
        revalidate();
        repaint();
    }
}
