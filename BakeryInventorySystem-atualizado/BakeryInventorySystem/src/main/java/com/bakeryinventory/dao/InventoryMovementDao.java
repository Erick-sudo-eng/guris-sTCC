package com.bakeryinventory.dao;

import com.bakeryinventory.config.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InventoryMovementDao {
    public void record(Integer productId, Integer userId, String movementType, int quantityChange, String notes) throws SQLException {
        String sql = """
                INSERT INTO inventory_movements(product_id, user_id, movement_type, quantity_change, notes)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (productId == null) {
                statement.setNull(1, java.sql.Types.INTEGER);
            } else {
                statement.setInt(1, productId);
            }
            if (userId == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, userId);
            }
            statement.setString(3, movementType);
            statement.setInt(4, quantityChange);
            statement.setString(5, notes);
            statement.executeUpdate();
        }
    }
}
