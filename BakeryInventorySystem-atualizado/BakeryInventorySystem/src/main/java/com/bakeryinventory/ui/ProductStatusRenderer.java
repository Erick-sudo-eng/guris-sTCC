package com.bakeryinventory.ui;

import com.bakeryinventory.model.Product;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

public class ProductStatusRenderer extends DefaultTableCellRenderer {
    private final ProductTableModel model;

    public ProductStatusRenderer(ProductTableModel model) {
        this.model = model;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (!isSelected) {
            int modelRow = table.convertRowIndexToModel(row);
            Product product = model.getProductAt(modelRow);
            if (product.isExpired()) {
                component.setBackground(UiTheme.DANGER_BG);
            } else if (product.isNearExpiration(7)) {
                component.setBackground(UiTheme.WARNING_BG);
            } else if (product.isBelowIdealStock()) {
                component.setBackground(UiTheme.STOCK_BG);
            } else {
                component.setBackground(UiTheme.PANEL);
            }
        }
        return component;
    }
}
