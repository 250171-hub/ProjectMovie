package org.example._version;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserProfile {

    // --- DARK THEME COLORS ---
    private static final String BLACK = "#121212";
    private static final String DARK_GRAY = "#1E1E1E";
    private static final String YELLOW = "#FFD700";
    private static final String BORDER_COLOR = "#333333";
    private static final String FIELD_BG = "#252525";
    private static final String LIGHT_TEXT = "#CCCCCC";

    // Dynamic Notifications
    public static class Notification {
        public String type;
        public String description;
        public Notification(String t, String d) { type=t; description=d; }
    }
    public static java.util.ArrayList<Notification> userNotifications = new java.util.ArrayList<>(java.util.Arrays.asList(
            new Notification("Welcome", "Welcome to Cinema Reserve! Start booking tickets to earn coins.")
    ));

    public static void display(Stage mainStage, ImageView navAvatarIcon) {
        mainStage.hide();

        Stage profileStage = new Stage();
        profileStage.setTitle("Cinema Reserve | Your Profile");

        HBox rootLayout = new HBox();
        rootLayout.setStyle("-fx-background-color: " + BLACK + ";");

        // ==========================================
        // 1. SIDEBAR COLUMN (Left)
        // ==========================================
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20, 40, 20, 40));
        sidebar.setMinWidth(350);
        sidebar.setMaxWidth(350);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setStyle("-fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 0 1 0 0;");

        Button backBtn = new Button("← BACK");
        backBtn.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + YELLOW + "; -fx-border-color: " + YELLOW + "; -fx-border-radius: 5; -fx-cursor: hand; -fx-padding: 5 10;");
        backBtn.setOnAction(e -> {
            profileStage.close();
            mainStage.show();
        });
        HBox backBox = new HBox(backBtn);
        backBox.setAlignment(Pos.TOP_LEFT);
        backBox.setPadding(new Insets(0, 0, 0, -20));

        // Avatar
        Image currentAvatar = navAvatarIcon.getImage();
        ImageView profileImageView = new ImageView(currentAvatar);
        profileImageView.setFitWidth(120);
        profileImageView.setFitHeight(120);
        profileImageView.setClip(new Circle(60, 60, 60));

        Button changePhotoBtn = new Button("CHANGE PHOTO");
        changePhotoBtn.setMaxWidth(Double.MAX_VALUE);
        changePhotoBtn.setStyle("-fx-background-color: " + BORDER_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8;");
        changePhotoBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File file = fileChooser.showOpenDialog(profileStage);
            if (file != null) {
                Image newAvatar = new Image(file.toURI().toString());
                profileImageView.setImage(newAvatar);
                navAvatarIcon.setImage(newAvatar);
            }
        });
        VBox avatarBox = new VBox(10, profileImageView, changePhotoBtn);
        avatarBox.setAlignment(Pos.CENTER);

        // --- NEW: BALANCE & COINS WALLET ---
        HBox walletBox = new HBox(15);
        walletBox.setAlignment(Pos.CENTER);
        walletBox.setStyle("-fx-background-color: " + DARK_GRAY + "; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 15;");

        VBox balanceBox = new VBox(5);
        balanceBox.setAlignment(Pos.CENTER);
        Label balLbl = new Label("BALANCE");
        balLbl.setStyle("-fx-text-fill: " + LIGHT_TEXT + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label balAmt = new Label("$45.00"); // Dummy static balance
        balAmt.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        balanceBox.getChildren().addAll(balLbl, balAmt);

        VBox coinBox = new VBox(5);
        coinBox.setAlignment(Pos.CENTER);
        Label coinLbl = new Label("COINS");
        coinLbl.setStyle("-fx-text-fill: " + LIGHT_TEXT + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label coinAmt = new Label("🪙 120"); // Dummy static coins
        coinAmt.setStyle("-fx-text-fill: " + YELLOW + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        coinBox.getChildren().addAll(coinLbl, coinAmt);

        Region walletSpacer = new Region();
        HBox.setHgrow(walletSpacer, Priority.ALWAYS);
        walletBox.getChildren().addAll(balanceBox, walletSpacer, coinBox);

        // Form Fields
        GridPane grid = new GridPane();
        grid.setVgap(12);
        grid.setHgap(10);
        grid.setAlignment(Pos.CENTER);
        String labelStyle = "-fx-text-fill: " + LIGHT_TEXT + "; -fx-font-weight: bold;";
        String fieldStyle = "-fx-background-color: " + FIELD_BG + "; -fx-text-fill: white; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 5; -fx-padding: 8;";

        grid.addRow(0, createLabel("Name", labelStyle));
        grid.addRow(1, createTextField("User", fieldStyle));
        grid.addRow(2, createLabel("Surname", labelStyle));
        grid.addRow(3, createTextField("User", fieldStyle));

        Button editProfileBtn = new Button("SAVE PROFILE");
        editProfileBtn.setMaxWidth(Double.MAX_VALUE);
        editProfileBtn.setStyle("-fx-background-color: " + YELLOW + "; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 12;");

        sidebar.getChildren().addAll(backBox, avatarBox, walletBox, grid, editProfileBtn);

        // ==========================================
        // 2. TICKETS COLUMN (Middle - DATABASE LINKED)
        // ==========================================
        VBox ticketsColumn = new VBox(20);
        ticketsColumn.setPadding(new Insets(20));
        HBox.setHgrow(ticketsColumn, Priority.ALWAYS);

        Label ticketsTitle = new Label("My Tickets");
        ticketsTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
        ticketsTitle.setTextFill(Color.web(YELLOW));

        ScrollPane ticketsScroll = new ScrollPane();
        ticketsScroll.setFitToWidth(true);
        ticketsScroll.setStyle("-fx-background: " + BLACK + "; -fx-background-color: " + BLACK + "; -fx-border-width: 0;");

        VBox ticketsList = new VBox(15);
        ticketsList.setPadding(new Insets(0, 10, 0, 0));
        ticketsList.setStyle("-fx-background-color: " + BLACK + ";");

        // Load tickets from Database!
        loadTicketsFromDatabase(ticketsList);

        ticketsScroll.setContent(ticketsList);
        ticketsColumn.getChildren().addAll(ticketsTitle, ticketsScroll);

        // ==========================================
        // 3. NOTIFICATIONS COLUMN (Right)
        // ==========================================
        VBox notificationsColumn = new VBox(20);
        notificationsColumn.setPadding(new Insets(20));
        HBox.setHgrow(notificationsColumn, Priority.ALWAYS);

        Label notificationsTitle = new Label("Notifications");
        notificationsTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
        notificationsTitle.setTextFill(Color.web(YELLOW));

        ScrollPane notificationsScroll = new ScrollPane();
        notificationsScroll.setFitToWidth(true);
        notificationsScroll.setStyle("-fx-background: " + BLACK + "; -fx-background-color: " + BLACK + "; -fx-border-width: 0;");

        VBox notificationsList = new VBox(15);
        notificationsList.setPadding(new Insets(0, 10, 0, 0));
        notificationsList.setStyle("-fx-background-color: " + BLACK + ";");

        for (Notification n : userNotifications) {
            notificationsList.getChildren().add(createNotificationCard(n.type, n.description));
        }
        notificationsScroll.setContent(notificationsList);
        notificationsColumn.getChildren().addAll(notificationsTitle, notificationsScroll);

        rootLayout.getChildren().addAll(sidebar, ticketsColumn, notificationsColumn);
        Scene scene = new Scene(rootLayout, 1300, 900);
        profileStage.setScene(scene);
        profileStage.setOnCloseRequest(e -> mainStage.show());
        profileStage.show();
    }

    // --- HELPER METHODS ---

    private static Label createLabel(String text, String style) {
        Label l = new Label(text);
        l.setStyle(style);
        return l;
    }

    private static TextField createTextField(String text, String style) {
        TextField f = new TextField(text);
        f.setStyle(style);
        return f;
    }

    // --- DATABASE TICKET FETCHER ---
    private static void loadTicketsFromDatabase(VBox ticketsList) {
        // Clear old ones if we are refreshing
        ticketsList.getChildren().clear();

        String query = "SELECT t.ticket_id, t.movie_title, t.price, b.booking_date, b.booking_id " +
                "FROM booked_tickets t " +
                "JOIN bookings b ON t.booking_id = b.booking_id " +
                "ORDER BY b.booking_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int ticketId = rs.getInt("ticket_id");
                String movie = rs.getString("movie_title");
                double price = rs.getDouble("price");
                String date = rs.getString("booking_date"); // Timestamp
                String bkgId = rs.getString("booking_id");

                HBox card = createTicketCard(ticketId, movie, bkgId, price, date, ticketsList);
                ticketsList.getChildren().add(card);
            }

            if (ticketsList.getChildren().isEmpty()) {
                Label noTickets = new Label("No tickets booked yet. Go grab some!");
                noTickets.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                ticketsList.getChildren().add(noTickets);
            }

        } catch (SQLException e) {
            System.out.println("Error loading tickets: " + e.getMessage());
            Label errorLbl = new Label("Error connecting to database.");
            errorLbl.setTextFill(Color.RED);
            ticketsList.getChildren().add(errorLbl);
        }
    }

    // --- TICKET CARD UI (WITH CANCEL BUTTON) ---
    private static HBox createTicketCard(int ticketId, String movie, String bookingId, double price, String date, VBox parentList) {
        HBox card = new HBox();
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: " + DARK_GRAY + "; -fx-background-radius: 10; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 10;");

        // Left Side
        VBox leftInfo = new VBox(8);
        Label title = new Label(movie);
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        title.setTextFill(Color.WHITE);

        HBox detailsRow = new HBox(15);
        Label idLabel = new Label("🎫 ID: " + bookingId);
        Label dateLabel = new Label("🕒 " + date);
        idLabel.setTextFill(Color.web(LIGHT_TEXT));
        dateLabel.setTextFill(Color.web(LIGHT_TEXT));
        detailsRow.getChildren().addAll(idLabel, dateLabel);

        leftInfo.getChildren().addAll(title, detailsRow);

        // Right Side (Price & Cancel Button)
        VBox rightInfo = new VBox(10);
        rightInfo.setAlignment(Pos.CENTER_RIGHT);
        Label p = new Label("$" + String.format("%.2f", price));
        p.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        p.setTextFill(Color.web(YELLOW));

        Button cancelBtn = new Button("Cancel Ticket");
        cancelBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 5;");
        cancelBtn.setOnAction(e -> cancelTicket(ticketId, card, parentList));

        rightInfo.getChildren().addAll(p, cancelBtn);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(leftInfo, spacer, rightInfo);
        return card;
    }

    // --- DATABASE TICKET DELETER ---
    private static void cancelTicket(int ticketId, HBox cardUI, VBox parentList) {
        String deleteQuery = "DELETE FROM booked_tickets WHERE ticket_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {

            stmt.setInt(1, ticketId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Remove the UI card immediately
                parentList.getChildren().remove(cardUI);

                // Check if list is empty now
                if (parentList.getChildren().isEmpty()) {
                    Label noTickets = new Label("No tickets booked yet. Go grab some!");
                    noTickets.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                    parentList.getChildren().add(noTickets);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not cancel ticket: " + e.getMessage());
            alert.show();
        }
    }

    private static VBox createNotificationCard(String type, String description) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: " + DARK_GRAY + "; -fx-background-radius: 10; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 10;");

        Label boldType = new Label("🔔 " + type);
        boldType.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        boldType.setTextFill(Color.web(YELLOW));

        Label desc = new Label(description);
        desc.setFont(Font.font(13));
        desc.setTextFill(Color.WHITE);
        desc.setWrapText(true);

        card.getChildren().addAll(boldType, desc);
        return card;
    }
}