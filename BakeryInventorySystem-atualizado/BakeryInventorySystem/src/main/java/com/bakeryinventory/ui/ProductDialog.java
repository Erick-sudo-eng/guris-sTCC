package com.bakeryinventory.ui;

import com.bakeryinventory.model.Product;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ProductDialog extends JDialog {
    private final JTextField nameField = new JTextField(22);
    private final JTextField categoryField = new JTextField(22);
    private final JTextField quantityField = new JTextField(22);
    private final JTextField idealQuantityField = new JTextField(22);
    private final JTextField unitPriceField = new JTextField(22);
    private final JTextField expirationDateField = new JTextField(22);
    private final JTextField registrationDateField = new JTextField(22);
    private Product product;
    private boolean saved;

    public ProductDialog(MainFrame owner, Product product) {
        super(owner, product == null ? "Adicionar produto" : "Editar produto", true);
        this.product = product == null ? new Product() : product;
        build();
        fill();
    }

    public boolean isSaved() {
        return saved;
    }

    public Product getProduct() {
        return product;
    }

    private void build() {
        setSize(430, 450);
        setLocationRelativeTo(getOwner());

        JPanel root = new JPanel(new BorderLayout(10, 10));
        UiTheme.pad(root);

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setOpaque(false);
        form.add(new LabeledField("Nome do produto", nameField));
        form.add(new LabeledField("Categoria", categoryField));
        form.add(new LabeledField("Quantidade em estoque", quantityField));
        form.add(new LabeledField("Estoque ideal", idealQuantityField));
        form.add(new LabeledField("Preco unitario", unitPriceField));
        form.add(new LabeledField("Data de validade (yyyy-MM-dd)", expirationDateField));
        form.add(new LabeledField("Data de cadastro (yyyy-MM-dd)", registrationDateField));
        root.add(form, BorderLayout.CENTER);

        JLabel hint = new JLabel("Use datas no formato yyyy-MM-dd.");
        root.add(hint, BorderLayout.NORTH);

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(event -> dispose());
        JButton saveButton = UiTheme.primaryButton("Salvar");
        saveButton.addActionListener(event -> save());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(cancelButton);
        actions.add(saveButton);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void fill() {
        nameField.setText(product.getName() == null ? "" : product.getName());
        categoryField.setText(product.getCategory() == null ? "" : product.getCategory());
        quantityField.setText(String.valueOf(product.getQuantity()));
        idealQuantityField.setText(String.valueOf(product.getIdealQuantity()));
        unitPriceField.setText(product.getUnitPrice() == null ? "0.00" : product.getUnitPrice().toPlainString());
        expirationDateField.setText(product.getExpirationDate() == null ? LocalDate.now().plusDays(7).toString() : product.getExpirationDate().toString());
        registrationDateField.setText(product.getRegistrationDate() == null ? LocalDate.now().toString() : product.getRegistrationDate().toString());
    }

    private void save() {
        try {
            product.setName(nameField.getText());
            product.setCategory(categoryField.getText());
            product.setQuantity(Integer.parseInt(quantityField.getText().trim()));
            product.setIdealQuantity(Integer.parseInt(idealQuantityField.getText().trim()));
            product.setUnitPrice(new BigDecimal(unitPriceField.getText().trim().replace(",", ".")));
            product.setExpirationDate(LocalDate.parse(expirationDateField.getText().trim()));
            product.setRegistrationDate(LocalDate.parse(registrationDateField.getText().trim()));
            saved = true;
            dispose();
        } catch (Exception ex) {
            Dialogs.error(this, new IllegalArgumentException("Confira os numeros e datas. As datas devem usar yyyy-MM-dd."));
        }
    }
}
