package org.example._version;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookingService {

    /**
     * Books a seat, saves it to the database, and deducts the price from the user.
     */
    public static boolean bookSeat(String bookingId, String movieTitle, String seatNumber, double price) {
        int currentUserId = UserSession.getInstance().getUserId();

        String insertTicketSQL = "INSERT INTO booked_tickets (user_id, booking_id, movie_title, seat_number, price) VALUES (?, ?, ?, ?, ?)";
        String deductBalanceSQL = "UPDATE users SET coins_balance = coins_balance - ? WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Start Transaction

            try (PreparedStatement ticketStmt = conn.prepareStatement(insertTicketSQL);
                 PreparedStatement paymentStmt = conn.prepareStatement(deductBalanceSQL)) {

                // 1. Insert the booked ticket
                ticketStmt.setInt(1, currentUserId);
                ticketStmt.setString(2, bookingId);
                ticketStmt.setString(3, movieTitle);
                ticketStmt.setString(4, seatNumber);
                ticketStmt.setDouble(5, price);
                ticketStmt.executeUpdate();

                // 2. Deduct the price from the user's wallet
                paymentStmt.setDouble(1, price);
                paymentStmt.setInt(2, currentUserId);
                paymentStmt.executeUpdate();

                // 3. Commit the transaction
                conn.commit();

                // 4. Update the local session balance so the UI updates immediately
                double currentBalance = UserSession.getInstance().getCoinsBalance();
                UserSession.getInstance().setCoinsBalance(currentBalance - price);

                return true;

            } catch (SQLException e) {
                conn.rollback(); // Undo if it fails
                System.err.println("Booking failed, transaction rolled back.");
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cancels a ticket, deletes it from the database, and ONLY refunds if before the deadline.
     */
    public static boolean cancelTicket(int ticketId, int userId, double ticketPrice, boolean isBeforeDeadline) {
        String deleteTicketSQL = "DELETE FROM booked_tickets WHERE ticket_id = ?";
        String refundBalanceSQL = "UPDATE users SET coins_balance = coins_balance + ? WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Start Transaction

            try {
                // 1. Delete the ticket (This ALWAYS happens when canceled)
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteTicketSQL)) {
                    deleteStmt.setInt(1, ticketId);
                    int rowsDeleted = deleteStmt.executeUpdate();

                    if (rowsDeleted == 0) {
                        throw new SQLException("Ticket not found.");
                    }
                }

                // 2. Refund the user (ONLY happens if before the deadline)
                if (isBeforeDeadline) {
                    try (PreparedStatement refundStmt = conn.prepareStatement(refundBalanceSQL)) {
                        refundStmt.setDouble(1, ticketPrice);
                        refundStmt.setInt(2, userId);
                        refundStmt.executeUpdate();
                    }

                    // Update local session balance immediately
                    double currentBalance = UserSession.getInstance().getCoinsBalance();
                    UserSession.getInstance().setCoinsBalance(currentBalance + ticketPrice);
                }

                // 3. Commit the transaction
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Cancellation failed, transaction rolled back.");
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all booked tickets for the currently logged-in user.
     */
    public static void loadUserTickets(VBox parentContainer) {
        int currentUserId = UserSession.getInstance().getUserId();
        parentContainer.getChildren().clear();

        // 👉 1. Added show_time to the SELECT query
        String query = "SELECT ticket_id, booking_id, movie_title, seat_number, price, show_time FROM booked_tickets WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            boolean hasTickets = false;

            while (rs.next()) {
                hasTickets = true;
                int ticketId = rs.getInt("ticket_id");
                String bookingId = rs.getString("booking_id");
                String title = rs.getString("movie_title");
                String seat = rs.getString("seat_number");
                double price = rs.getDouble("price");

                // 👉 2. Pull the date string from the database
                String showTime = rs.getString("show_time");

                // 👉 3. Pass the showTime into your UI builder method
                // Note: Ensure your create method in UserProfile is updated to accept this String!
// We removed the 'seat' variable and put 'title' before 'bookingId'
                UserProfile.createTicketCard(ticketId, title, bookingId, price, showTime, parentContainer);
            }

            if (!hasTickets) {
                Label noTickets = new Label("No tickets booked yet. Go grab some!");
                parentContainer.getChildren().add(noTickets);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
