package org.example._version;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MovieCatalog {

    /**
     * Searches and filters movies based on provided criteria.
     * * @param keyword   Search text for the movie title (can be empty).
     * @param genre     The specific Genre to filter by, or Genre.ALL for no filter.
     * @param city      The city to filter by, or "All Cities".
     * @param minRating The minimum rating required (0.0 to 10.0).
     * @return A list of Movie objects matching the criteria.
     */
    public List<Movie> searchAndFilter(String keyword, Genre genre, String city, double minRating) {
        List<Movie> searchResults = new ArrayList<>();

        // 1. Build the SQL Query dynamically based on active filters
        StringBuilder sqlQuery = new StringBuilder("SELECT * FROM movies WHERE rating >= ?");

        if (genre != null && genre != Genre.ALL) {
            sqlQuery.append(" AND genre = ?");
        }
        if (city != null && !city.equalsIgnoreCase("All Cities")) {
            sqlQuery.append(" AND location = ?");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sqlQuery.append(" AND title LIKE ?");
        }

        // 2. Execute Query using the centralized DatabaseManager
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery.toString())) {

            // Bind parameters dynamically
            int parameterIndex = 1;
            statement.setDouble(parameterIndex++, minRating);

            if (genre != null && genre != Genre.ALL) {
                statement.setString(parameterIndex++, genre.name());
            }
            if (city != null && !city.equalsIgnoreCase("All Cities")) {
                statement.setString(parameterIndex++, city);
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                statement.setString(parameterIndex++, "%" + keyword.trim() + "%");
            }

            // 3. Process the Result Set
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Movie movie = extractMovieFromResultSet(resultSet);
                    searchResults.add(movie);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Database Error while searching movies: " + e.getMessage());
            e.printStackTrace();
        }

        return searchResults;
    }

    /**
     * Helper method to map a single database row into a Movie object.
     * (Adheres to the Single Responsibility Principle)
     */
    private Movie extractMovieFromResultSet(ResultSet rs) throws SQLException {
        return new Movie(
                rs.getString("title"),
                Genre.valueOf(rs.getString("genre")),
                rs.getDouble("price"),
                rs.getString("image_url"),
                rs.getString("location"),
                rs.getInt("release_year"),
                rs.getDouble("rating"),
                rs.getString("showtimes")
        );
    }
}
