package com.bakeryinventory.dao;

import com.bakeryinventory.config.Database;
import com.bakeryinventory.model.Product;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDao {
    public List<Product> search(String filter) throws SQLException {
        List<Product> products = new ArrayList<>();
        String pattern = "%" + (filter == null ? "" : filter.trim()) + "%";
        String sql = """
                SELECT id, name, category, quantity, ideal_quantity, unit_price, expiration_date, registration_date
                FROM products
                WHERE name LIKE ? OR category LIKE ? OR CAST(id AS TEXT) LIKE ?
                ORDER BY expiration_date ASC, name ASC
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(map(resultSet));
                }
            }
        }
        return products;
    }

    public List<Product> findNearExpiration(int days) throws SQLException {
        return findByExpirationCondition("expiration_date BETWEEN date('now') AND date('now', '+' || ? || ' day')", days);
    }

    public List<Product> findExpired() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = """
                SELECT id, name, category, quantity, ideal_quantity, unit_price, expiration_date, registration_date
                FROM products
                WHERE expiration_date < date('now')
                ORDER BY expiration_date ASC
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                products.add(map(resultSet));
            }
        }
        return products;
    }

    private List<Product> findByExpirationCondition(String condition, int days) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = """
                SELECT id, name, category, quantity, ideal_quantity, unit_price, expiration_date, registration_date
                FROM products
                WHERE %s
                ORDER BY expiration_date ASC
                """.formatted(condition);
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, days);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(map(resultSet));
                }
            }
        }
        return products;
    }

    public Optional<Product> findById(int id) throws SQLException {
        String sql = """
                SELECT id, name, category, quantity, ideal_quantity, unit_price, expiration_date, registration_date
                FROM products
                WHERE id = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public int insert(Product product) throws SQLException {
        String sql = """
                INSERT INTO products(name, category, quantity, ideal_quantity, unit_price, expiration_date, registration_date)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(statement, product);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public void update(Product product) throws SQLException {
        String sql = """
                UPDATE products
                SET name = ?, category = ?, quantity = ?, ideal_quantity = ?, unit_price = ?,
                    expiration_date = ?, registration_date = ?, updated_at = datetime('now')
                WHERE id = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, product);
            statement.setInt(8, product.getId());
            statement.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private void fillStatement(PreparedStatement statement, Product product) throws SQLException {
        statement.setString(1, product.getName().trim());
        statement.setString(2, product.getCategory().trim());
        statement.setInt(3, product.getQuantity());
        statement.setInt(4, product.getIdealQuantity());
        statement.setBigDecimal(5, product.getUnitPrice());
        statement.setString(6, product.getExpirationDate().toString());
        statement.setString(7, product.getRegistrationDate().toString());
    }

    private Product map(ResultSet resultSet) throws SQLException {
        return new Product(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("category"),
                resultSet.getInt("quantity"),
                resultSet.getInt("ideal_quantity"),
                BigDecimal.valueOf(resultSet.getDouble("unit_price")),
                LocalDate.parse(resultSet.getString("expiration_date")),
                LocalDate.parse(resultSet.getString("registration_date"))
        );
    }
}
