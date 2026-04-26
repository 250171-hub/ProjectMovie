package org.example._version;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CouponService {

    /**
     * Checks the database for a matching active coupon code.
     * @param couponCode The code entered by the user.
     * @return The discount percentage (e.g., 10.0 for 10%), or 0.0 if invalid/inactive.
     */
    public static double getDiscountPercentage(String couponCode) {
        // We only want to select the percentage IF the coupon matches AND is active
        String query = "SELECT discount_percentage FROM coupons WHERE coupon_code = ? AND is_active = TRUE";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Convert input to uppercase so "cinema10" and "CINEMA10" both work
            stmt.setString(1, couponCode.toUpperCase().trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Coupon found and active! Return the percentage.
                    return rs.getDouble("discount_percentage");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Database Error while validating coupon.");
            e.printStackTrace();
        }

        // If we get here, the coupon doesn't exist, is inactive, or a DB error occurred
        return 0.0;
    }
}
