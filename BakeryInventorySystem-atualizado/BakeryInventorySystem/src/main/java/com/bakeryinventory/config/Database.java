package com.bakeryinventory.config;

import com.bakeryinventory.dao.UserDao;
import com.bakeryinventory.service.PasswordService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class Database {
    private static final Path DATA_DIR = Path.of("data");
    private static final String JDBC_URL = "jdbc:sqlite:" + DATA_DIR.resolve("bakery_inventory.db");

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(JDBC_URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static void initialize() throws SQLException, IOException {
        Files.createDirectories(DATA_DIR);
        executeSqlResource("/db/schema.sql");
        ensureDefaultManager();
    }

    private static void executeSqlResource(String resourcePath) throws SQLException, IOException {
        String sql = readResource(resourcePath);
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            for (String command : splitSqlStatements(sql)) {
                String trimmed = command.trim();
                if (!trimmed.isEmpty()) {
                    statement.execute(trimmed);
                }
            }
        }
    }

    private static List<String> splitSqlStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inTrigger = false;

        for (String line : sql.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }

            String upper = trimmed.toUpperCase();
            if (upper.startsWith("CREATE TRIGGER")) {
                inTrigger = true;
            }

            current.append(line).append('\n');

            boolean statementEnded = inTrigger ? upper.equals("END;") : trimmed.endsWith(";");
            if (statementEnded) {
                statements.add(current.toString());
                current.setLength(0);
                inTrigger = false;
            }
        }

        if (!current.toString().trim().isEmpty()) {
            statements.add(current.toString());
        }
        return statements;
    }

    private static String readResource(String resourcePath) throws IOException {
        try (InputStream input = Database.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
    }

    private static void ensureDefaultManager() throws SQLException {
        UserDao userDao = new UserDao();
        if (!userDao.managerExists()) {
            PasswordService.HashResult hash = PasswordService.hashPassword("12345678");
            userDao.createUser("gerente", hash.hash(), hash.salt(), "MANAGER");
        }
    }
}
