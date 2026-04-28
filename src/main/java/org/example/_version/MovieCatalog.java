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
     *
     * @param keyword    Search text for the movie title (can be empty).
     * @param genre      The specific Genre to filter by, or Genre.ALL for no filter.
     * @param city       The city to filter by, or "All Cities".
     * @param minRating  The minimum rating required (0.0 to 10.0).
     * @return A list of Movie objects matching the criteria.
     */
    public List<Movie> searchAndFilter(String keyword, Genre genre, String city, double minRating) {
        return searchAndFilter(keyword, genre, city, minRating, "All Years", "All Languages");
    }

    /**
     * Full overload: includes year and language filters.
     * Called by MovieBookingApp's loadMoviesAsync().
     *
     * @param keyword      Search text for the movie title (can be empty).
     * @param genre        The specific Genre to filter by, or Genre.ALL for no filter.
     * @param city         The city to filter by, or "All Cities".
     * @param minRating    The minimum rating required (0.0 to 10.0).
     * @param year         "All Years" or a 4-digit year string e.g. "2023".
     * @param language     "All Languages" or a specific language e.g. "English".
     * @return A list of Movie objects matching all criteria.
     */
    public List<Movie> searchAndFilter(String keyword, Genre genre, String city,
                                       double minRating, String year, String language) {
        List<Movie> searchResults = new ArrayList<>();

        // ----------------------------------------------------------------
        // 1. Build the SQL query dynamically based on active filters
        // ----------------------------------------------------------------
        StringBuilder sql = new StringBuilder("SELECT * FROM movies WHERE rating >= ?");

        boolean filterGenre    = genre    != null && genre != Genre.ALL;
        boolean filterCity     = city     != null && !city.equalsIgnoreCase("All Cities");
        boolean filterKeyword  = keyword  != null && !keyword.trim().isEmpty();
        boolean filterYear     = year     != null && !year.equalsIgnoreCase("All Years");
        boolean filterLanguage = language != null && !language.equalsIgnoreCase("All Languages");

        if (filterGenre)    sql.append(" AND genre = ?");
        if (filterCity)     sql.append(" AND location = ?");
        if (filterKeyword)  sql.append(" AND title LIKE ?");
        if (filterYear)     sql.append(" AND release_year = ?");
        if (filterLanguage) sql.append(" AND language = ?");

        // ----------------------------------------------------------------
        // 2. Execute query using the centralized DatabaseManager
        // ----------------------------------------------------------------
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            int idx = 1;
            statement.setDouble(idx++, minRating);

            if (filterGenre)    statement.setString(idx++, genre.name());
            if (filterCity)     statement.setString(idx++, city);
            if (filterKeyword)  statement.setString(idx++, "%" + keyword.trim() + "%");
            if (filterYear)     statement.setInt(idx++, Integer.parseInt(year));
            if (filterLanguage) statement.setString(idx++, language);

            // ----------------------------------------------------------------
            // 3. Map each result row to a Movie object
            // ----------------------------------------------------------------
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    searchResults.add(extractMovieFromResultSet(rs));
                }
            }

        } catch (NumberFormatException e) {
            // Protects against a non-numeric year string being passed in
            System.err.println("⚠️ Invalid year filter value: " + year);
        } catch (SQLException e) {
            System.err.println("❌ Database error while searching movies: " + e.getMessage());
            e.printStackTrace();
        }

        return searchResults;
    }

    /**
     * Maps a single database row into a Movie object.
     * Reads the new 'language' column added to the movies table.
     */
    private Movie extractMovieFromResultSet(ResultSet rs) throws SQLException {
        return new Movie(
                rs.getString("title"),
                Genre.valueOf(rs.getString("genre").toUpperCase()),
                rs.getDouble("price"),
                rs.getString("image_url"),
                rs.getString("location"),
                rs.getInt("release_year"),
                rs.getDouble("rating"),
                rs.getString("showtimes"),
                rs.getString("language")      // ← new field
        );
    }
}
