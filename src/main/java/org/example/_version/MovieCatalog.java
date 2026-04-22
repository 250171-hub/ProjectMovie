package org.example._version;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class MovieCatalog {
    // Database Credentials - Update these to match your teammate's MySQL setup
    private final String URL = "jdbc:mysql://localhost:3306/movie_db";
    private final String USER = "root";
    private final String PASS = "PASSWORD";

    public List<Movie> searchAndFilter(String kw, Genre g, String city, double minRating) {
        List<Movie> results = new ArrayList<>();

        // Build the SQL Query dynamically
        StringBuilder sql = new StringBuilder("SELECT * FROM movies WHERE rating >= ?");

        if (g != Genre.ALL) sql.append(" AND genre = ?");
        if (city != null && !city.equals("All Cities")) sql.append(" AND location = ?");
        if (kw != null && !kw.isEmpty()) sql.append(" AND title LIKE ?");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            pstmt.setDouble(idx++, minRating);

            if (g != Genre.ALL) pstmt.setString(idx++, g.name());
            if (city != null && !city.equals("All Cities")) pstmt.setString(idx++, city);
            if (kw != null && !kw.isEmpty()) pstmt.setString(idx++, "%" + kw + "%");

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // This creates Movie objects using data fetched from MySQL
                results.add(new Movie(
                        rs.getString("title"),
                        Genre.valueOf(rs.getString("genre")),
                        rs.getDouble("price"),
                        rs.getString("image_url"),
                        rs.getString("location"),
                        rs.getInt("release_year"),
                        rs.getDouble("rating"),
                        rs.getString("showtimes")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
            // Optional: return a dummy list so the app doesn't crash if DB is down
        }
        return results;
    }
}
