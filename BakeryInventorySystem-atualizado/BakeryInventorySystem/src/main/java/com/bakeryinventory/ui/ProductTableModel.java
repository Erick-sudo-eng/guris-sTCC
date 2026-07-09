package com.bakeryinventory.ui;

import com.bakeryinventory.model.Product;

import javax.swing.table.AbstractTableModel;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProductTableModel extends AbstractTableModel {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final String[] columns = {"ID", "Produto", "Categoria", "Estoque", "Ideal", "Preco", "Validade", "Cadastro", "Situacao"};
    private List<Product> products = new ArrayList<>();

    public void setProducts(List<Product> products) {
        this.products = new ArrayList<>(products);
        fireTableDataChanged();
    }

    public Product getProductAt(int row) {
        return products.get(row);
    }

    @Override
    public int getRowCount() {
        return products.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Product product = products.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> product.getId();
            case 1 -> product.getName();
            case 2 -> product.getCategory();
            case 3 -> product.getQuantity();
            case 4 -> product.getIdealQuantity();
            case 5 -> "R$ " + product.getUnitPrice().setScale(2, RoundingMode.HALF_UP);
            case 6 -> product.getExpirationDate().format(DATE_FORMAT);
            case 7 -> product.getRegistrationDate().format(DATE_FORMAT);
            case 8 -> status(product);
            default -> "";
        };
    }

    private String status(Product product) {
        if (product.isExpired()) {
            return "Vencido";
        }
        if (product.isNearExpiration(7) && product.isBelowIdealStock()) {
            return "Estoque baixo + validade proxima";
        }
        if (product.isNearExpiration(7)) {
            return "Validade proxima";
        }
        if (product.isBelowIdealStock()) {
            return "Abaixo do estoque ideal";
        }
        return "OK";
    }
}
