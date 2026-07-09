package com.bakeryinventory.ui;

import com.bakeryinventory.model.Product;
import com.bakeryinventory.model.User;
import com.bakeryinventory.service.AuthService;
import com.bakeryinventory.service.InventoryService;
import com.bakeryinventory.service.ReportService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MainFrame extends JFrame {
    private final User currentUser;
    private final InventoryService inventoryService = new InventoryService();
    private final AuthService authService = new AuthService();
    private final ReportService reportService = new ReportService();

    private final JTextField searchField = new JTextField(24);
    private final ProductTableModel productModel = new ProductTableModel();
    private final JTable productTable = new JTable(productModel);
    private final ProductTableModel nearExpirationModel = new ProductTableModel();
    private final ProductTableModel expiredModel = new ProductTableModel();
    private final UserTableModel userModel = new UserTableModel();
    private JTable userTable;
    private JLabel summaryLabel;
    private JTextField bakeryNameField;
    private JComboBox<String> reportTypeCombo;

    public MainFrame(User currentUser) {
        super("Sistema de Estoque da Padaria");
        this.currentUser = currentUser;
        build();
        refreshAll();
        showStartupAlerts();
    }

    private void build() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 740);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        UiTheme.pad(root);

        root.add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Estoque", buildProductsPanel());
        tabs.addTab("Validade", buildExpirationPanel());
        if (currentUser.isManager()) {
            tabs.addTab("Usuarios", buildUsersPanel());
            tabs.addTab("Relatorios", buildReportsPanel());
        }
        root.add(tabs, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Estoque da Padaria");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        JLabel user = new JLabel("Usuario logado: " + currentUser.getUsername() + " (" + roleLabel(currentUser) + ")");
        header.add(title, BorderLayout.WEST);
        header.add(user, BorderLayout.EAST);
        return header;
    }

    private JPanel buildProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout(8, 8));
        toolbar.setOpaque(false);
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        search.setOpaque(false);
        search.add(new JLabel("Buscar"));
        search.add(searchField);
        JButton searchButton = new JButton("Atualizar");
        searchButton.addActionListener(event -> refreshAll());
        search.add(searchButton);
        toolbar.add(search, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        JButton addButton = UiTheme.primaryButton("Adicionar");
        addButton.addActionListener(event -> addProduct());
        JButton editButton = new JButton("Editar selecionado");
        editButton.addActionListener(event -> editProduct());
        JButton deleteButton = UiTheme.dangerButton("Excluir selecionado");
        deleteButton.addActionListener(event -> deleteProduct());
        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);
        toolbar.add(actions, BorderLayout.EAST);

        panel.add(toolbar, BorderLayout.NORTH);

        configureProductTable(productTable);
        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        summaryLabel = new JLabel(" ");
        panel.add(summaryLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildExpirationPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.setOpaque(false);

        JTable nearTable = new JTable(nearExpirationModel);
        configureProductTable(nearTable);
        JTable expiredTable = new JTable(expiredModel);
        configureProductTable(expiredTable);

        panel.add(section("Produtos proximos da validade", nearTable));
        panel.add(section("Produtos vencidos", expiredTable));
        return panel;
    }

    private JPanel buildUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton addButton = UiTheme.primaryButton("Criar usuario");
        addButton.addActionListener(event -> createUser());
        JButton deleteButton = UiTheme.dangerButton("Excluir usuario");
        deleteButton.addActionListener(event -> deleteUser());
        actions.add(addButton);
        actions.add(deleteButton);
        panel.add(actions, BorderLayout.NORTH);

        userTable = new JTable(userModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setOpaque(false);
        bakeryNameField = new JTextField(30);
        try {
            bakeryNameField.setText(reportService.getBakeryName());
        } catch (Exception ex) {
            bakeryNameField.setText("Padaria Modelo");
        }
        reportTypeCombo = new JComboBox<>(reportService.reportTypes().toArray(String[]::new));
        form.add(new LabeledField("Nome da padaria", bakeryNameField));
        JPanel comboPanel = new JPanel(new BorderLayout(4, 4));
        comboPanel.setOpaque(false);
        comboPanel.add(new JLabel("Tipo de relatorio"), BorderLayout.NORTH);
        comboPanel.add(reportTypeCombo, BorderLayout.CENTER);
        form.add(comboPanel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton saveName = new JButton("Salvar nome");
        saveName.addActionListener(event -> saveBakeryName());
        JButton pdf = UiTheme.primaryButton("Exportar PDF");
        pdf.addActionListener(event -> exportReport("pdf"));
        JButton docx = UiTheme.primaryButton("Exportar Word");
        docx.addActionListener(event -> exportReport("docx"));
        actions.add(saveName);
        actions.add(pdf);
        actions.add(docx);

        JLabel info = new JLabel("<html>Area exclusiva do gerente. Os relatorios incluem nome da padaria, data, usuario logado, tabelas e totais.</html>");
        panel.add(info, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel section(String title, JTable table) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        UiTheme.card(panel);
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void configureProductTable(JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setDefaultRenderer(Object.class, new ProductStatusRenderer((ProductTableModel) table.getModel()));
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(8).setPreferredWidth(210);
        TableRowSorter<?> sorter = (TableRowSorter<?>) table.getRowSorter();
        sorter.setSortKeys(List.of(new RowSorter.SortKey(6, SortOrder.ASCENDING)));
    }

    private Product selectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        return productModel.getProductAt(productTable.convertRowIndexToModel(selectedRow));
    }

    private void addProduct() {
        ProductDialog dialog = new ProductDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            try {
                inventoryService.save(dialog.getProduct(), currentUser);
                refreshAll();
                Dialogs.info(this, "Produto salvo com sucesso.");
            } catch (Exception ex) {
                Dialogs.error(this, ex);
            }
        }
    }

    private void editProduct() {
        Product selected = selectedProduct();
        if (selected == null) {
            Dialogs.info(this, "Selecione um produto na tabela para editar.");
            return;
        }
        Product copy = new Product(
                selected.getId(), selected.getName(), selected.getCategory(), selected.getQuantity(),
                selected.getIdealQuantity(), selected.getUnitPrice(), selected.getExpirationDate(),
                selected.getRegistrationDate()
        );
        ProductDialog dialog = new ProductDialog(this, copy);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            try {
                inventoryService.save(dialog.getProduct(), currentUser);
                refreshAll();
                Dialogs.info(this, "Produto atualizado com sucesso.");
            } catch (Exception ex) {
                Dialogs.error(this, ex);
            }
        }
    }

    private void deleteProduct() {
        Product selected = selectedProduct();
        if (selected == null) {
            Dialogs.info(this, "Selecione um produto na tabela para excluir.");
            return;
        }
        if (Dialogs.confirm(this, "Deseja realmente excluir o produto \"" + selected.getName() + "\"?")) {
            try {
                inventoryService.delete(selected, currentUser);
                refreshAll();
                Dialogs.info(this, "Produto excluido com sucesso.");
            } catch (Exception ex) {
                Dialogs.error(this, ex);
            }
        }
    }

    private void createUser() {
        UserDialog dialog = new UserDialog(this);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            try {
                authService.createUser(currentUser, dialog.getUsername(), dialog.getPassword());
                refreshUsers();
                Dialogs.info(this, "Usuario criado com sucesso.");
            } catch (Exception ex) {
                Dialogs.error(this, ex);
            }
        }
    }

    private void deleteUser() {
        int selectedRow = userTable == null ? -1 : userTable.getSelectedRow();
        if (selectedRow < 0) {
            Dialogs.info(this, "Selecione um usuario para excluir.");
            return;
        }
        User selected = userModel.getUserAt(userTable.convertRowIndexToModel(selectedRow));
        if (selected.isManager()) {
            Dialogs.info(this, "A conta gerente permanente nao pode ser excluida.");
            return;
        }
        if (Dialogs.confirm(this, "Deseja realmente excluir o usuario \"" + selected.getUsername() + "\"?")) {
            try {
                authService.deleteUser(currentUser, selected.getId());
                refreshUsers();
                Dialogs.info(this, "Usuario excluido com sucesso.");
            } catch (Exception ex) {
                Dialogs.error(this, ex);
            }
        }
    }

    private void saveBakeryName() {
        try {
            persistBakeryName();
            Dialogs.info(this, "Nome da padaria salvo com sucesso.");
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
    }

    private void persistBakeryName() throws Exception {
        reportService.setBakeryName(bakeryNameField.getText());
    }

    private void exportReport(String extension) {
        if (!currentUser.isManager()) {
            Dialogs.info(this, "Somente o gerente pode exportar relatorios.");
            return;
        }
        try {
            persistBakeryName();
            String type = (String) reportTypeCombo.getSelectedItem();
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Salvar relatorio");
            chooser.setFileFilter(new FileNameExtensionFilter(extension.equals("pdf") ? "PDF" : "Documento Word", extension));
            chooser.setSelectedFile(new java.io.File(type.toLowerCase().replace(" ", "_") + "." + extension));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            Path file = chooser.getSelectedFile().toPath();
            if (!file.toString().toLowerCase().endsWith("." + extension)) {
                file = Path.of(file + "." + extension);
            }
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            ReportService.ReportData data = reportService.buildReport(type, currentUser);
            if ("pdf".equals(extension)) {
                reportService.exportPdf(data, file);
            } else {
                reportService.exportDocx(data, file);
            }
            Dialogs.info(this, "Relatorio exportado com sucesso:\n" + file);
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
    }

    private void refreshAll() {
        refreshProducts();
        refreshExpiration();
        refreshUsers();
    }

    private void refreshProducts() {
        try {
            List<Product> products = inventoryService.search(searchField.getText());
            productModel.setProducts(products);
            long lowStock = products.stream().filter(Product::isBelowIdealStock).count();
            long expired = products.stream().filter(Product::isExpired).count();
            long near = products.stream().filter(product -> product.isNearExpiration(InventoryService.EXPIRATION_WARNING_DAYS)).count();
            summaryLabel.setText("Produtos: " + products.size() + " | Abaixo do ideal: " + lowStock
                    + " | Validade proxima: " + near + " | Vencidos: " + expired);
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
    }

    private void refreshExpiration() {
        try {
            nearExpirationModel.setProducts(inventoryService.nearExpiration());
            expiredModel.setProducts(inventoryService.expired());
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
    }

    private void refreshUsers() {
        if (!currentUser.isManager()) {
            return;
        }
        try {
            userModel.setUsers(authService.listUsers());
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
    }

    private void showStartupAlerts() {
        try {
            int near = inventoryService.nearExpiration().size();
            int expired = inventoryService.expired().size();
            if (near > 0 || expired > 0) {
                Dialogs.info(this, "Alerta de validade:\n" + near + " produto(s) proximos da validade.\n"
                        + expired + " produto(s) vencidos.");
            }
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
    }

    private String roleLabel(User user) {
        return user.isManager() ? "Gerente" : "Usuario";
    }
}
