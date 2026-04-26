package org.example._version;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserProfile {

    // --- DARK THEME CONSTANTS ---
    private static final String COLOR_BLACK = "#121212";
    private static final String COLOR_DARK_GRAY = "#1E1E1E";
    private static final String COLOR_YELLOW = "#FFD700";
    private static final String COLOR_BORDER = "#333333";
    private static final String COLOR_FIELD_BG = "#252525";
    private static final String COLOR_LIGHT_TEXT = "#CCCCCC";
    private static final String COLOR_ERROR_RED = "#8B0000";

    // --- NOTIFICATION STATE ---
    public static class Notification {
        private final String title;
        private final String description;

        public Notification(String title, String description) {
            this.title = title;
            this.description = description;
        }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }

    // Using a private static list with a public method to add notifications safely
    private static final List<Notification> userNotifications = new ArrayList<>(Arrays.asList(
            new Notification("Welcome", "Welcome to Cinema Reserve! Start booking tickets to earn coins.")
    ));

    public static void addNotification(String title, String description) {
        userNotifications.add(0, new Notification(title, description));
    }

    // ==========================================
    // MAIN ENTRY POINT
    // ==========================================

    public static void display(Stage mainStage, ImageView navAvatarIcon) {
        mainStage.hide();

        Stage profileStage = new Stage();
        profileStage.setTitle("Cinema Reserve | Your Profile");

        HBox rootLayout = new HBox();
        rootLayout.setStyle("-fx-background-color: " + COLOR_BLACK + ";");

        // Break down UI construction into smaller, maintainable methods
        VBox sidebar = buildSidebar(profileStage, mainStage, navAvatarIcon);
        VBox ticketsColumn = buildTicketsColumn();
        VBox notificationsColumn = buildNotificationsColumn();

        rootLayout.getChildren().addAll(sidebar, ticketsColumn, notificationsColumn);

        Scene scene = new Scene(rootLayout, 1300, 900);
        profileStage.setScene(scene);
        profileStage.setOnCloseRequest(e -> mainStage.show());
        profileStage.show();
    }

    // ==========================================
    // UI BUILDER METHODS
    // ==========================================
    private static VBox buildSidebar(Stage profileStage, Stage mainStage, ImageView navAvatarIcon) {
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20, 40, 20, 40));
        sidebar.setMinWidth(350);
        sidebar.setMaxWidth(350);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setStyle("-fx-border-color: " + COLOR_BORDER + "; -fx-border-width: 0 1 0 0;");

        // Back Button
        Button backBtn = new Button("← BACK");
        backBtn.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + COLOR_YELLOW + "; -fx-border-color: " + COLOR_YELLOW + "; -fx-border-radius: 5; -fx-cursor: hand; -fx-padding: 5 10;");
        backBtn.setOnAction(e -> {
            profileStage.close();
            mainStage.show();
        });
        HBox backBox = new HBox(backBtn);
        backBox.setAlignment(Pos.TOP_LEFT);
        backBox.setPadding(new Insets(0, 0, 0, -20));

        // User Avatar
        Image currentAvatar = navAvatarIcon.getImage();
        ImageView profileImageView = new ImageView(currentAvatar);
        profileImageView.setFitWidth(120);
        profileImageView.setFitHeight(120);
        profileImageView.setClip(new Circle(60, 60, 60));

        Button changePhotoBtn = new Button("CHANGE PHOTO");
        changePhotoBtn.setMaxWidth(Double.MAX_VALUE);
        changePhotoBtn.setStyle("-fx-background-color: " + COLOR_BORDER + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8;");
        changePhotoBtn.setOnAction(e -> handleAvatarChange(profileStage, profileImageView, navAvatarIcon));

        VBox avatarBox = new VBox(10, profileImageView, changePhotoBtn);
        avatarBox.setAlignment(Pos.CENTER);

        // Coin Wallet Module
        HBox coinContainer = new HBox(15);
        coinContainer.setAlignment(Pos.CENTER);
        coinContainer.setStyle("-fx-background-color: " + COLOR_DARK_GRAY + "; -fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 15;");

        VBox coinBox = new VBox(5);
        coinBox.setAlignment(Pos.CENTER);
        Label coinLbl = new Label("COLLECTED COINS");
        coinLbl.setStyle("-fx-text-fill: " + COLOR_LIGHT_TEXT + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label coinAmt = new Label("🪙 " + UserSession.getInstance().getCoinsBalance());
        coinAmt.setStyle("-fx-text-fill: " + COLOR_YELLOW + "; -fx-font-size: 22px; -fx-font-weight: bold;");
        coinBox.getChildren().addAll(coinLbl, coinAmt);
        coinContainer.getChildren().add(coinBox);

        // Fields
        String fieldStyle = "-fx-background-color: " + COLOR_FIELD_BG + "; -fx-text-fill: white; -fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 5; -fx-padding: 8;";

        TextField nameField = createStyledTextField(UserSession.getInstance().getFirstName() != null ? UserSession.getInstance().getFirstName() : "", fieldStyle);
        nameField.setEditable(false);
        nameField.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #333333;");

        TextField surnameField = createStyledTextField(UserSession.getInstance().getLastName() != null ? UserSession.getInstance().getLastName() : "", fieldStyle);
        surnameField.setEditable(false);
        surnameField.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #333333;");

        TextField emailField = createStyledTextField(UserSession.getInstance().getEmail() != null ? UserSession.getInstance().getEmail() : "", fieldStyle);
        emailField.setEditable(false);
        emailField.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #333333;");

        TextField phoneField = createStyledTextField(UserSession.getInstance().getPhone() != null ? UserSession.getInstance().getPhone() : "", fieldStyle);
        phoneField.setEditable(false);
        phoneField.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #333333;");

        GridPane formGrid = buildUserDetailsForm(nameField, surnameField, emailField, phoneField);

        // Edit / Save Toggle Button
        Button editSaveBtn = new Button("EDIT PROFILE");
        editSaveBtn.setMaxWidth(Double.MAX_VALUE);
        editSaveBtn.setStyle("-fx-background-color: " + COLOR_YELLOW + "; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10; -fx-background-radius: 5;");

        editSaveBtn.setOnAction(e -> {
            if (editSaveBtn.getText().equals("EDIT PROFILE")) {
                nameField.setEditable(true);
                surnameField.setEditable(true);
                phoneField.setEditable(true);
                emailField.setEditable(true);

                String activeFieldStyle = "-fx-background-color: " + COLOR_FIELD_BG + "; -fx-text-fill: white; -fx-border-color: " + COLOR_YELLOW + ";";
                nameField.setStyle(activeFieldStyle);
                surnameField.setStyle(activeFieldStyle);
                phoneField.setStyle(activeFieldStyle);
                emailField.setStyle(activeFieldStyle);

                editSaveBtn.setText("SAVE PROFILE");
            } else {
                String name = nameField.getText().trim();
                String surname = surnameField.getText().trim();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();

                boolean isSaved = UserService.saveUserProfile(name, surname, phone, email);

                if (isSaved) {
                    nameField.setEditable(false);
                    surnameField.setEditable(false);
                    phoneField.setEditable(false);
                    emailField.setEditable(false);

                    String lockedStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #333333;";
                    nameField.setStyle(lockedStyle);
                    surnameField.setStyle(lockedStyle);
                    phoneField.setStyle(lockedStyle);
                    emailField.setStyle(lockedStyle);

                    editSaveBtn.setText("EDIT PROFILE");

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Profile updated successfully!");
                    successAlert.setHeaderText(null);
                    successAlert.show();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to save profile. Please check your connection.");
                    errorAlert.setHeaderText(null);
                    errorAlert.show();
                }
            }
        });

        // 👉 NEW: LOG OUT BUTTON
        Button logoutBtn = new Button("LOG OUT");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + COLOR_ERROR_RED + "; -fx-font-weight: bold; -fx-border-color: " + COLOR_ERROR_RED + "; -fx-border-radius: 5; -fx-cursor: hand; -fx-padding: 10;");

        logoutBtn.setOnAction(e -> {
            // 1. Wipe the user session data
            if (UserSession.getInstance() != null) {
                UserSession.getInstance().cleanUserSession();
            }

            // 2. Clear out user notifications so the next person doesn't see them
            userNotifications.clear();

            // 3. Close BOTH screens (the profile and the old main screen)
            profileStage.close();
            mainStage.close();

            // 👉 4. Launch a completely fresh Main screen!
            // IMPORTANT: Replace "Main" with the actual name of your starting class
            // (It might be called App, HelloApplication, CinemaMain, etc.)
            try {
                new MovieBookingApp().start(new Stage());
            } catch (Exception ex) {
                System.err.println("Could not reload the main screen.");
                ex.printStackTrace();
            }

            System.out.println("User logged out and screen refreshed!");
        });

        // Added the logoutBtn to the very bottom of the sidebar
        sidebar.getChildren().addAll(backBox, avatarBox, coinContainer, formGrid, editSaveBtn, logoutBtn);
        return sidebar;
    }

    private static GridPane buildUserDetailsForm(TextField nameField, TextField surnameField, TextField emailField, TextField phoneField) {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setAlignment(Pos.CENTER);

        String labelStyle = "-fx-text-fill: " + COLOR_LIGHT_TEXT + "; -fx-font-weight: bold;";

        grid.addRow(0, createStyledLabel("Name", labelStyle));
        grid.addRow(1, nameField);

        grid.addRow(2, createStyledLabel("Surname", labelStyle));
        grid.addRow(3, surnameField);

        grid.addRow(4, createStyledLabel("Email", labelStyle));
        grid.addRow(5, emailField);

        grid.addRow(6, createStyledLabel("Phone Number", labelStyle));
        grid.addRow(7, phoneField);

        return grid;
    }

    private static VBox buildTicketsColumn() {
        VBox ticketsColumn = new VBox(20);
        ticketsColumn.setPadding(new Insets(20));
        HBox.setHgrow(ticketsColumn, Priority.ALWAYS);

        Label ticketsTitle = new Label("My Tickets");
        ticketsTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
        ticketsTitle.setTextFill(Color.web(COLOR_YELLOW));

        ScrollPane ticketsScroll = new ScrollPane();
        ticketsScroll.setFitToWidth(true);
        ticketsScroll.setStyle("-fx-background: " + COLOR_BLACK + "; -fx-background-color: " + COLOR_BLACK + "; -fx-border-width: 0;");

        VBox ticketsList = new VBox(15);
        ticketsList.setPadding(new Insets(0, 10, 0, 0));
        ticketsList.setStyle("-fx-background-color: " + COLOR_BLACK + ";");

        // Populate with Database Data
        fetchAndLoadTickets(ticketsList);

        ticketsScroll.setContent(ticketsList);
        ticketsColumn.getChildren().addAll(ticketsTitle, ticketsScroll);
        return ticketsColumn;
    }

    private static VBox buildNotificationsColumn() {
        VBox notificationsColumn = new VBox(20);
        notificationsColumn.setPadding(new Insets(20));
        HBox.setHgrow(notificationsColumn, Priority.ALWAYS);

        Label notificationsTitle = new Label("Notifications");
        notificationsTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
        notificationsTitle.setTextFill(Color.web(COLOR_YELLOW));

        ScrollPane notificationsScroll = new ScrollPane();
        notificationsScroll.setFitToWidth(true);
        notificationsScroll.setStyle("-fx-background: " + COLOR_BLACK + "; -fx-background-color: " + COLOR_BLACK + "; -fx-border-width: 0;");

        VBox notificationsList = new VBox(15);
        notificationsList.setPadding(new Insets(0, 10, 0, 0));
        notificationsList.setStyle("-fx-background-color: " + COLOR_BLACK + ";");

        for (Notification notification : userNotifications) {
            notificationsList.getChildren().add(createNotificationCard(notification));
        }

        notificationsScroll.setContent(notificationsList);
        notificationsColumn.getChildren().addAll(notificationsTitle, notificationsScroll);
        return notificationsColumn;
    }

    // ==========================================
    // DATABASE METHODS
    // ==========================================

    private static void fetchAndLoadTickets(VBox ticketsList) {
        ticketsList.getChildren().clear();
        String query = "SELECT t.ticket_id, t.movie_title, t.price, b.booking_date, b.booking_id " +
                "FROM booked_tickets t " +
                "JOIN bookings b ON t.booking_id = b.booking_id " +
                "ORDER BY b.booking_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                int ticketId = resultSet.getInt("ticket_id");
                String movieTitle = resultSet.getString("movie_title");
                double ticketPrice = resultSet.getDouble("price");
                String bookingDate = resultSet.getString("booking_date");
                String bookingId = resultSet.getString("booking_id");

                HBox ticketCard = createTicketCard(ticketId, movieTitle, bookingId, ticketPrice, bookingDate, ticketsList);
                ticketsList.getChildren().add(ticketCard);
            }

            if (ticketsList.getChildren().isEmpty()) {
                Label noTicketsLabel = new Label("No tickets booked yet. Go grab some!");
                noTicketsLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                ticketsList.getChildren().add(noTicketsLabel);
            }

        } catch (SQLException e) {
            System.err.println("Error loading tickets: " + e.getMessage());
        }
    }

    private static void deleteTicketRecord(int ticketId, double ticketPrice, String movieDateString, HBox uiCard, VBox parentContainer) {
        int currentUserId = UserSession.getInstance().getUserId();

        // 👉 1. DETERMINE IF REFUND IS ALLOWED
        boolean isBeforeDeadline = false;

        try {
            // Example: Assuming your movie date/time in the database looks like "2024-05-20 18:30:00"
            // We check if the current time is BEFORE the deadline (e.g., 2 hours before the movie)
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            java.time.LocalDateTime movieTime = java.time.LocalDateTime.parse(movieDateString, formatter);
            java.time.LocalDateTime deadline = movieTime.minusHours(2); // Deadline is 2 hours before show

            isBeforeDeadline = java.time.LocalDateTime.now().isBefore(deadline);
        } catch (Exception e) {
            System.out.println("Could not parse date to check deadline, defaulting to NO refund.");
        }

        // 👉 2. ASK THE USER FOR CONFIRMATION
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Cancel Ticket");
        if (isBeforeDeadline) {
            confirmDialog.setHeaderText("Refund Available!");
            confirmDialog.setContentText("You are canceling before the deadline. You will be refunded $" + ticketPrice + ". Proceed?");
        } else {
            confirmDialog.setHeaderText("Late Cancellation (No Refund)");
            confirmDialog.setContentText("You have missed the cancellation deadline. Your ticket will be canceled, but NO money will be refunded. Proceed?");
        }

        final boolean finalIsBeforeDeadline = isBeforeDeadline;
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {

                // 👉 3. PROCESS CANCELLATION IN DATABASE
                // Note: You might need to update your BookingService method to accept the boolean so it knows whether to refund or not
                boolean success = BookingService.cancelTicket(ticketId, currentUserId, ticketPrice, finalIsBeforeDeadline);

                if (success) {
                    parentContainer.getChildren().remove(uiCard);

                    if (finalIsBeforeDeadline) {
                        addNotification("Ticket Canceled", "Your ticket was canceled and $" + ticketPrice + " was refunded.");
                    } else {
                        addNotification("Ticket Canceled", "Your ticket was canceled. No refund was issued due to late cancellation.");
                    }

                    if (parentContainer.getChildren().isEmpty()) {
                        Label noTicketsLabel = new Label("No tickets booked yet. Go grab some!");
                        noTicketsLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                        parentContainer.getChildren().add(noTicketsLabel);
                    }
                } else {
                    Alert error = new Alert(Alert.AlertType.ERROR, "Could not cancel ticket. Database error.");
                    error.show();
                }
            }
        });
    }

    // ==========================================
    // HELPER UI COMPONENTS
    // ==========================================

    private static void handleAvatarChange(Stage stage, ImageView profileImage, ImageView navImage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            Image newAvatar = new Image(file.toURI().toString());
            profileImage.setImage(newAvatar);
            navImage.setImage(newAvatar);
        }
    }

    private static Label createStyledLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        return label;
    }

    private static TextField createStyledTextField(String text, String style) {
        TextField field = new TextField(text);
        field.setStyle(style);
        return field;
    }

    public static HBox createTicketCard(int ticketId, String movieTitle, String bookingId, double price, String date, VBox parentContainer) {
        HBox card = new HBox();
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: " + COLOR_DARK_GRAY + "; -fx-background-radius: 10; -fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 10;");

        // Left Information Column
        VBox leftInfo = new VBox(8);
        Label titleLabel = new Label(movieTitle);
        titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);

        HBox detailsRow = new HBox(15);
        Label idLabel = new Label("🎫 ID: " + bookingId);
        Label dateLabel = new Label("🕒 " + date);
        idLabel.setTextFill(Color.web(COLOR_LIGHT_TEXT));
        dateLabel.setTextFill(Color.web(COLOR_LIGHT_TEXT));
        detailsRow.getChildren().addAll(idLabel, dateLabel);

        leftInfo.getChildren().addAll(titleLabel, detailsRow);

        // Right Information Column (Price & Action)
        VBox rightInfo = new VBox(10);
        rightInfo.setAlignment(Pos.CENTER_RIGHT);

        Label priceLabel = new Label("$" + String.format("%.2f", price));
        priceLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        priceLabel.setTextFill(Color.web(COLOR_YELLOW));

        Button cancelBtn = new Button("Cancel Ticket");
        cancelBtn.setStyle("-fx-background-color: " + COLOR_ERROR_RED + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 5;");

        // 👉 1. Make unchangeable "final" copies for Java's strict lambda rules
        final int finalTicketId = ticketId;
        final double finalPrice = price;
        final String finalDate = date;  // Pulls the REAL date from your method parameters!
        final HBox finalCard = card;
        final VBox finalContainer = parentContainer;

        // 👉 2. Pass the final copies into the button click
        cancelBtn.setOnAction(e -> deleteTicketRecord(finalTicketId, finalPrice, finalDate, finalCard, finalContainer));

        rightInfo.getChildren().addAll(priceLabel, cancelBtn);

        // Layout Assembly
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        card.getChildren().addAll(leftInfo, spacer, rightInfo);

        return card;
    }

    private static VBox createNotificationCard(Notification notification) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: " + COLOR_DARK_GRAY + "; -fx-background-radius: 10; -fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 10;");

        Label titleLabel = new Label("🔔 " + notification.getTitle());
        titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web(COLOR_YELLOW));

        Label descLabel = new Label(notification.getDescription());
        descLabel.setFont(Font.font(13));
        descLabel.setTextFill(Color.WHITE);
        descLabel.setWrapText(true);

        card.getChildren().addAll(titleLabel, descLabel);
        return card;
    }

}