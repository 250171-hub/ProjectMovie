package org.example._version;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    // Change "cinema_db" to whatever you named your database!
    private static final String URL = "jdbc:mysql://localhost:3306/movie_db";
    private static final String USER = "root";       // Default MySQL username
    private static final String PASSWORD = "PASSWORD"; // YOUR MySQL password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
