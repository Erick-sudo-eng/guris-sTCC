package com.bakeryinventory.model;

import java.time.LocalDateTime;

public class User {
    private final int id;
    private final String username;
    private final String passwordHash;
    private final String salt;
    private final String role;
    private final LocalDateTime createdAt;

    public User(int id, String username, String passwordHash, String salt, String role, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isManager() {
        return "MANAGER".equals(role);
    }
}
