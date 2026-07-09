package com.bakeryinventory.dao;

import com.bakeryinventory.config.Database;
import com.bakeryinventory.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    private static final DateTimeFormatter DB_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public boolean managerExists() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'MANAGER'";
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next() && resultSet.getInt(1) > 0;
        }
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public void createUser(String username, String passwordHash, String salt, String role) throws SQLException {
        String sql = "INSERT INTO users(username, password_hash, salt, role) VALUES (?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            statement.setString(2, passwordHash);
            statement.setString(3, salt);
            statement.setString(4, role);
            statement.executeUpdate();
        }
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, salt, role, created_at FROM users WHERE username = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password_hash, salt, role, created_at FROM users ORDER BY role, username";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(map(resultSet));
            }
        }
        return users;
    }

    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ? AND role <> 'MANAGER'";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
    }

    private User map(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("id"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                resultSet.getString("salt"),
                resultSet.getString("role"),
                LocalDateTime.parse(resultSet.getString("created_at"), DB_DATE_TIME)
        );
    }
}
