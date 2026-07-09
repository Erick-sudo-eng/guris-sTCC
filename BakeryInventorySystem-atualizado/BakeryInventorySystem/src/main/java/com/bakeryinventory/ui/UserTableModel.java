package com.bakeryinventory.ui;

import com.bakeryinventory.model.User;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UserTableModel extends AbstractTableModel {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final String[] columns = {"ID", "Usuario", "Perfil", "Criado em"};
    private List<User> users = new ArrayList<>();

    public void setUsers(List<User> users) {
        this.users = new ArrayList<>(users);
        fireTableDataChanged();
    }

    public User getUserAt(int row) {
        return users.get(row);
    }

    @Override
    public int getRowCount() {
        return users.size();
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
        User user = users.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> user.getId();
            case 1 -> user.getUsername();
            case 2 -> user.isManager() ? "Gerente" : "Usuario";
            case 3 -> user.getCreatedAt().format(DATE_FORMAT);
            default -> "";
        };
    }
}
