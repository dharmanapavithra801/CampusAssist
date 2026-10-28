package com.campusassist.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Opens connections to MySQL.
 * Settings come from environment variables so passwords never sit inside the source code.
 */
public final class DBConnection {

    private static final String URL = env("DB_URL", "jdbc:mysql://localhost:3306/campusassist");
    private static final String USER = env("DB_USER", "root");
    private static final String PASSWORD = env("DB_PASSWORD", "");

    private DBConnection() { }   // nobody should create an instance of a utility class

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
