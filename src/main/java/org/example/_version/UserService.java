package org.example._version;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserService {

    /**
     * Updates the currently logged-in user's profile in the database.
     */
    public static boolean saveUserProfile(String firstName, String lastName, String phone, String email) {
        int currentUserId = UserSession.getInstance().getUserId();
        String updateSQL = "UPDATE users SET first_name = ?, last_name = ?, phone = ?, email = ? WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSQL)) {

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, phone);
            stmt.setString(4, email);
            stmt.setInt(5, currentUserId);

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                // If DB save was successful, update the active session too!
                UserSession session = UserSession.getInstance();
                session.setFirstName(firstName);
                session.setLastName(lastName);
                session.setPhone(phone);
                session.setEmail(email);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to save user profile.");
            e.printStackTrace();
        }
        return false;
    }
}
