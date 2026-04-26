package org.example._version;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieBookingApp extends Application {


    // --- STATE MANAGEMENT ---
    private final MovieCatalog catalog = new MovieCatalog();
    private final List<Movie> basket = new ArrayList<>();
    private final Map<Movie, Button> cardButtons = new HashMap<>();

    private String currentSearchKeyword = "";
    private String currentCityFilter = "All Cities";
    private int currentHeroImageIndex = 0;
    private List<Movie> recommendedMovies;

    // --- UI COMPONENTS ---
    private final FlowPane movieGrid = new FlowPane();
    private final ImageView heroImageView = new ImageView();
    private final Label heroTitle = new Label();
    private final Image userAvatarImage = new Image("https://cdn-icons-png.flaticon.com/512/149/149071.png", 150, 150, true, true);
    private final ImageView navAvatarIcon = new ImageView(userAvatarImage);

    // --- THEME CONSTANTS ---
    private static final String COLOR_BLACK = "#121212";
    private static final String COLOR_YELLOW = "#FFD700";
    private static final String COLOR_DARK_GRAY = "#1E1E1E";
    private static final String COLOR_SUCCESS_GREEN = "#4CAF50";
    private static final String COLOR_ERROR_RED = "#F44336";

    @Override
    public void start(Stage primaryStage) {


        // Simulates a logged-in user so the session knows who is saving the profile
        UserSession.getInstance().loginUser(1, "JohnDoe", 100.0, "", "", "", "");




        // 1. Setup Navigation Bar
        HBox navBar = createNavigationBar(primaryStage);

        // 2. Setup Hero Slider
        StackPane heroSlider = createHeroSlider();

        // 3. Setup Main Movie Grid
        ScrollPane mainScroll = createMainMovieGrid();

        // 4. Setup Floating Basket Button
        Button basketBtn = createBasketButton();

        // 5. Assemble Layout
        StackPane rootStack = new StackPane();
        VBox mainLayout = new VBox(navBar, heroSlider, mainScroll);
        rootStack.getChildren().addAll(mainLayout, basketBtn);
        StackPane.setAlignment(basketBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(basketBtn, new Insets(40));

        // 6. Load Initial Data
        initializeData();

        // 7. Render Scene
        Scene scene = new Scene(rootStack, 1300, 900);
        primaryStage.setTitle("Cinema Reserve | Premium Online Booking");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // ==========================================
    // UI BUILDER METHODS (Applying Single Responsibility)
    // ==========================================

    private HBox createNavigationBar(Stage primaryStage) {
        HBox navBar = new HBox(20);
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(15, 40, 15, 40));
        navBar.setStyle("-fx-background-color: " + COLOR_BLACK + "; -fx-border-color: #333333; -fx-border-width: 0 0 1 0;");

        navAvatarIcon.setFitWidth(40);
        navAvatarIcon.setFitHeight(40);
        navAvatarIcon.setClip(new javafx.scene.shape.Circle(20, 20, 20));

        Label logo = new Label("CINEMA RESERVE");
        logo.setFont(Font.font("Verdana", FontWeight.BOLD, 26));
        logo.setTextFill(Color.web(COLOR_YELLOW));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchBox = new TextField();
        searchBox.setPromptText("Search movies...");
        searchBox.setPrefWidth(200);
        searchBox.setStyle("-fx-background-color: #252525; -fx-text-fill: white; -fx-prompt-text-fill: #888888;");

        ComboBox<String> cityFilter = new ComboBox<>();
        cityFilter.getItems().setAll("All Cities", "Tashkent", "London", "New York");
        cityFilter.setValue("All Cities");
        cityFilter.setStyle("-fx-background-color: " + COLOR_YELLOW + "; -fx-font-weight: bold;");

        // Add event listeners for filtering
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> loadMovies(newValue, cityFilter.getValue()));
        cityFilter.setOnAction(e -> loadMovies(searchBox.getText(), cityFilter.getValue()));

// Create the buttons
        Button profileBtn = new Button();
        profileBtn.setGraphic(navAvatarIcon);
        profileBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0;");
        profileBtn.setOnAction(e -> UserProfile.display(primaryStage, navAvatarIcon));

        Button loginBtn = new Button("LOG IN");
        loginBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + COLOR_YELLOW + "; -fx-border-color: " + COLOR_YELLOW + "; -fx-border-radius: 5; -fx-font-weight: bold; -fx-padding: 5 15;");
        loginBtn.setOnAction(e -> goToTeammateLogin(primaryStage));

        // 👉 THE NEW LOGIC: Check if user is logged in (Assuming ID > 0 means logged in)
        boolean isLoggedIn = UserSession.getInstance().getUserId() > 0;

        loginBtn.setVisible(!isLoggedIn);
        loginBtn.setManaged(!isLoggedIn); // setManaged(false) means it won't take up blank space

        profileBtn.setVisible(isLoggedIn);
        profileBtn.setManaged(isLoggedIn);

        // Inside your Main or Home Screen file:

        int currentUserId = UserSession.getInstance().getUserId();

        if (currentUserId > 0) {
            // User IS logged in: Show the Avatar picture
            navBar.getChildren().add(navAvatarIcon);
        } else {
            // User IS NOT logged in: Show the Sign In button
            Button signInBtn = new Button("SIGN IN");
          // Sign in Button logic to sign in
            navBar.getChildren().add(signInBtn);
        }

        // Add them to the nav bar as usual
        navBar.getChildren().addAll(logo, spacer, searchBox, cityFilter, loginBtn, profileBtn);
        return navBar;
    }

    private StackPane createHeroSlider() {
        StackPane heroSlider = new StackPane();
        heroSlider.setPrefHeight(400);
        heroSlider.setStyle("-fx-background-color: black;");

        heroImageView.setFitHeight(400);
        heroImageView.setPreserveRatio(true);

        VBox overlay = new VBox(10);
        overlay.setAlignment(Pos.BOTTOM_LEFT);
        overlay.setPadding(new Insets(0, 0, 40, 60));
        overlay.setStyle("-fx-background-color: linear-gradient(to top, rgba(18,18,18,1), rgba(18,18,18,0));");

        heroTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 48));
        heroTitle.setTextFill(Color.WHITE);

        Button nextBtn = new Button("NEXT FEATURED ❯");
        nextBtn.setStyle("-fx-background-color: " + COLOR_YELLOW + "; -fx-font-weight: bold; -fx-padding: 10 20;");
        nextBtn.setOnAction(e -> rotateHeroImage());

        overlay.getChildren().addAll(heroTitle, nextBtn);
        heroSlider.getChildren().addAll(heroImageView, overlay);
        return heroSlider;
    }

    private ScrollPane createMainMovieGrid() {
        movieGrid.setHgap(30);
        movieGrid.setVgap(30);
        movieGrid.setPadding(new Insets(40));
        movieGrid.setStyle("-fx-background-color: " + COLOR_BLACK + ";");

        ScrollPane mainScroll = new ScrollPane(movieGrid);
        mainScroll.setFitToWidth(true);
        mainScroll.setStyle("-fx-background: " + COLOR_BLACK + "; -fx-background-color: " + COLOR_BLACK + "; -fx-border-color: " + COLOR_BLACK + ";");
        VBox.setVgrow(mainScroll, Priority.ALWAYS);

        // Bind grid width to scroll pane so items wrap responsively
        movieGrid.prefWrapLengthProperty().bind(mainScroll.widthProperty().subtract(80));
        return mainScroll;
    }

    private Button createBasketButton() {
        Button basketBtn = new Button("🛒");
        basketBtn.setStyle("-fx-background-color: " + COLOR_YELLOW + "; -fx-background-radius: 50; -fx-font-size: 28; -fx-pref-width: 75; -fx-pref-height: 75; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 15, 0, 0, 0);");
        basketBtn.setOnAction(e -> showBasket());
        return basketBtn;
    }

    // ==========================================
    // LOGIC & STATE METHODS
    // ==========================================

    private void initializeData() {
        recommendedMovies = catalog.searchAndFilter("", Genre.ALL, "All Cities", 0.0);
        Collections.shuffle(recommendedMovies);
        rotateHeroImage();
        loadMovies(currentSearchKeyword, currentCityFilter);
    }

    private void rotateHeroImage() {
        if (recommendedMovies == null || recommendedMovies.isEmpty()) return;

        Movie currentMovie = recommendedMovies.get(currentHeroImageIndex);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), heroImageView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.2);
        fadeOut.setOnFinished(e -> {
            heroImageView.setImage(new Image(currentMovie.getImageUrl(), 1200, 400, true, true));
            heroTitle.setText(currentMovie.getTitle().toUpperCase());

            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), heroImageView);
            fadeIn.setFromValue(0.2);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();

        currentHeroImageIndex = (currentHeroImageIndex + 1) % recommendedMovies.size();
    }

    private void loadMovies(String keyword, String city) {
        movieGrid.getChildren().clear();
        this.currentSearchKeyword = keyword;
        this.currentCityFilter = city;

        List<Movie> filteredMovies = catalog.searchAndFilter(keyword, Genre.ALL, city, 0.0);

        if (filteredMovies.isEmpty()) {
            Label noResults = new Label("No movies found in " + city + ". Try searching something else!");
            noResults.setTextFill(Color.GRAY);
            noResults.setFont(Font.font(20));
            movieGrid.getChildren().add(noResults);
        } else {
            for (Movie movie : filteredMovies) {
                movieGrid.getChildren().add(createMovieCard(movie));
            }
        }
    }

    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: " + COLOR_DARK_GRAY + "; -fx-background-radius: 15; -fx-border-color: #333333; -fx-border-radius: 15;");
        card.setPadding(new Insets(15));
        card.setPrefWidth(250);
        card.setAlignment(Pos.TOP_LEFT);

        ImageView poster = new ImageView(new Image(movie.getImageUrl(), 220, 310, true, true));
        poster.setStyle("-fx-background-radius: 10;");

        Label title = new Label(movie.getTitle().toUpperCase());
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 17));
        title.setWrapText(true);
        title.setMinHeight(50);

        Label details = new Label("⭐ " + movie.getRating() + " | " + movie.getGenre());
        details.setTextFill(Color.web(COLOR_YELLOW));

        Label times = new Label("🕒 " + movie.getTimes());
        times.setTextFill(Color.LIGHTGRAY);
        times.setFont(Font.font(12));

        Label priceLabel = new Label("$" + String.format("%.2f", movie.getPrice()));
        priceLabel.setTextFill(Color.WHITE);
        priceLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));

        Button bookBtn = new Button();
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        cardButtons.put(movie, bookBtn); // Track button for UI updates

        updateButtonVisuals(movie); // Set initial state (Added vs Book Now)

        bookBtn.setOnAction(e -> {
            if (!basket.contains(movie)) {
                basket.add(movie);
                updateButtonVisuals(movie);
            }
        });

        card.getChildren().addAll(poster, title, details, times, priceLabel, bookBtn);

        // Hover Effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #252525; -fx-background-radius: 15; -fx-border-color: " + COLOR_YELLOW + "; -fx-border-radius: 15;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: " + COLOR_DARK_GRAY + "; -fx-background-radius: 15; -fx-border-color: #333333; -fx-border-radius: 15;"));

        return card;
    }

    private void updateButtonVisuals(Movie movie) {
        Button btn = cardButtons.get(movie);
        if (btn == null) return;

        if (basket.contains(movie)) {
            btn.setText("ADDED ✓");
            btn.setStyle("-fx-background-color: " + COLOR_SUCCESS_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12;");
            btn.setDisable(true);
        } else {
            btn.setText("BOOK NOW");
            btn.setStyle("-fx-background-color: " + COLOR_YELLOW + "; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 12; -fx-cursor: hand;");
            btn.setDisable(false);
        }
    }

    // ==========================================
    // CHECKOUT & DIALOG METHODS
    // ==========================================

    private void showBasket() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Your Booked Tickets");

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: " + COLOR_DARK_GRAY + ";");
        content.setMinWidth(480);

        Label header = new Label("MY RESERVATIONS");
        header.setTextFill(Color.web(COLOR_YELLOW));
        header.setFont(Font.font("Verdana", FontWeight.BOLD, 22));
        content.getChildren().add(header);

        final boolean[] proceedToCheckout = {false};
        final double[] finalTotalToPay = {0.0};

        if (basket.isEmpty()) {
            Label emptyLabel = new Label("Your basket is empty.");
            emptyLabel.setTextFill(Color.GRAY);

            Button closeBtn = new Button("CLOSE");
            closeBtn.setMaxWidth(Double.MAX_VALUE);
            closeBtn.setStyle("-fx-background-color: " + COLOR_YELLOW + "; -fx-font-weight: bold;");
            closeBtn.setOnAction(e -> {
                dialog.setResult(null);
                dialog.close();
            });

            content.getChildren().addAll(emptyLabel, closeBtn);
        } else {
            double tempTotal = 0;

            for (Movie movie : basket) {
                HBox itemRow = new HBox(10);
                itemRow.setAlignment(Pos.CENTER_LEFT);

                Button removeBtn = new Button("✖");
                removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + COLOR_ERROR_RED + "; -fx-font-size: 14px; -fx-cursor: hand;");
                removeBtn.setOnAction(e -> {
                    basket.remove(movie);
                    updateButtonVisuals(movie);
                    dialog.setResult(null);
                    dialog.close();
                    Platform.runLater(this::showBasket); // Reload basket UI
                });

                Label movieName = new Label("• " + movie.getTitle());
                movieName.setTextFill(Color.WHITE);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label priceLabel = new Label("$" + String.format("%.2f", movie.getPrice()));
                priceLabel.setTextFill(Color.LIGHTGRAY);

                itemRow.getChildren().addAll(removeBtn, movieName, spacer, priceLabel);
                content.getChildren().add(itemRow);

                tempTotal += movie.getPrice();
            }

            final double initialTotal = tempTotal;
            finalTotalToPay[0] = initialTotal;

            Separator separator = new Separator();
            separator.setStyle("-fx-background-color: #333333;");

            // Coupon Section
            HBox couponBox = new HBox(10);
            couponBox.setAlignment(Pos.CENTER_LEFT);

            TextField couponField = new TextField();
            couponField.setPromptText("Enter code (e.g., CINEMA20)");
            couponField.setStyle("-fx-background-color: #252525; -fx-text-fill: white; -fx-border-color: #333333; -fx-border-radius: 3;");

            Button applyBtn = new Button("APPLY");
            applyBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-font-weight: bold;");

            Label discountMsg = new Label();
            discountMsg.setFont(Font.font(12));
            couponBox.getChildren().addAll(couponField, applyBtn, discountMsg);

            // Totals Section
            VBox totalBox = new VBox(5);
            totalBox.setAlignment(Pos.CENTER_RIGHT);
            Label originalTotalLabel = new Label("Initial Price: $" + String.format("%.2f", initialTotal));
            originalTotalLabel.setTextFill(Color.LIGHTGRAY);
            originalTotalLabel.setFont(Font.font("Verdana", 14));

            Label finalTotalLabel = new Label("TOTAL: $" + String.format("%.2f", initialTotal));
            finalTotalLabel.setTextFill(Color.web(COLOR_YELLOW));
            finalTotalLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));

            totalBox.getChildren().addAll(originalTotalLabel, finalTotalLabel);

            applyBtn.setOnAction(e -> {
                String enteredCode = couponField.getText().trim();

                if (enteredCode.isEmpty()) {
                    discountMsg.setText("Please enter a code.");
                    discountMsg.setTextFill(Color.web(COLOR_ERROR_RED));
                    return;
                }

                // 1. Call our database service to get the real discount
                double discountPercent = CouponService.getDiscountPercentage(enteredCode);

                if (discountPercent > 0) {
                    // 2. Calculate the discount based on the percentage from the DB
                    double discountAmount = initialTotal * (discountPercent / 100.0);
                    finalTotalToPay[0] = initialTotal - discountAmount;

                    // 3. Update the UI to show the successful discount
                    originalTotalLabel.setStyle("-fx-strikethrough: true; -fx-text-fill: gray;");
                    finalTotalLabel.setText("SALE TOTAL: $" + String.format("%.2f", finalTotalToPay[0]));
                    finalTotalLabel.setTextFill(Color.web(COLOR_SUCCESS_GREEN));

                    discountMsg.setText("-" + discountPercent + "% Applied!");
                    discountMsg.setTextFill(Color.web(COLOR_SUCCESS_GREEN));

                    // Lock the button so they can't apply multiple coupons
                    applyBtn.setDisable(true);
                    couponField.setDisable(true);
                } else {
                    // 4. If the DB returns 0.0, it's invalid or inactive
                    discountMsg.setText("Invalid or Expired Code");
                    discountMsg.setTextFill(Color.web(COLOR_ERROR_RED));
                }
            });

            content.getChildren().addAll(separator, couponBox, totalBox);

            // Action Buttons
            HBox buttonBox = new HBox(15);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(10, 0, 0, 0));

            Button cancelBtn = new Button("CANCEL");
            cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + COLOR_YELLOW + "; -fx-border-color: " + COLOR_YELLOW + "; -fx-border-radius: 5;");
            cancelBtn.setOnAction(e -> {
                dialog.setResult(null);
                dialog.close();
            });

            Button checkoutBtn = new Button("PROCEED TO CHECKOUT");
            checkoutBtn.setStyle("-fx-background-color: " + COLOR_YELLOW + "; -fx-font-weight: bold; -fx-text-fill: black; -fx-background-radius: 5;");
            checkoutBtn.setOnAction(e -> {
                proceedToCheckout[0] = true;
                dialog.setResult(null);
                dialog.close();
            });

            buttonBox.getChildren().addAll(cancelBtn, checkoutBtn);
            content.getChildren().add(buttonBox);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("-fx-background-color: " + COLOR_DARK_GRAY + ";");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);

        dialog.showAndWait();

        if (proceedToCheckout[0]) {
            showPaymentOptions(finalTotalToPay[0]);
        }
    }

    private void showPaymentOptions(double amountToPay) {
        Dialog<Void> payDialog = new Dialog<>();
        payDialog.setTitle("Secure Checkout");

        VBox content = new VBox(20);
        content.setPadding(new Insets(35));
        content.setStyle("-fx-background-color: " + COLOR_DARK_GRAY + "; -fx-border-color: " + COLOR_YELLOW + "; -fx-border-width: 2;");
        content.setAlignment(Pos.CENTER);

        Label header = new Label("SELECT PAYMENT METHOD");
        header.setTextFill(Color.WHITE);
        header.setFont(Font.font("Verdana", FontWeight.BOLD, 18));

        Label amountLabel = new Label("Amount Due: $" + String.format("%.2f", amountToPay));
        amountLabel.setTextFill(Color.web(COLOR_YELLOW));
        amountLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 24));

        Button cardBtn = createCheckoutButton("💳   PAY WITH CREDIT CARD");
        cardBtn.setOnAction(e -> {
            payDialog.close();
            Platform.runLater(() -> showNotificationOptions("Credit Card", amountToPay));
        });

        Button cashBtn = createCheckoutButton("💵   PAY AT CINEMA (CASH)");
        cashBtn.setOnAction(e -> {
            payDialog.close();
            Platform.runLater(() -> showNotificationOptions("Cash", amountToPay));
        });

        Button cancelBtn = new Button("← Back to Basket");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #888888; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> {
            payDialog.close();
            Platform.runLater(this::showBasket);
        });

        content.getChildren().addAll(header, amountLabel, cardBtn, cashBtn, cancelBtn);
        payDialog.getDialogPane().setContent(content);
        payDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        payDialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        payDialog.showAndWait();
    }

    private void showNotificationOptions(String paymentMethod, double amountToPay) {
        Dialog<Void> notifDialog = new Dialog<>();
        notifDialog.setTitle("Notification Preference");

        VBox content = new VBox(20);
        content.setPadding(new Insets(35));
        content.setStyle("-fx-background-color: " + COLOR_DARK_GRAY + "; -fx-border-color: " + COLOR_YELLOW + "; -fx-border-width: 2;");
        content.setAlignment(Pos.CENTER);

        Label header = new Label("HOW SHOULD WE SEND YOUR RECEIPT?");
        header.setTextFill(Color.WHITE);
        header.setFont(Font.font("Verdana", FontWeight.BOLD, 16));

        Button emailBtn = createCheckoutButton("📧   SEND VIA EMAIL");
        emailBtn.setOnAction(e -> {
            notifDialog.close();
            Platform.runLater(() -> processDatabaseCheckout(paymentMethod, amountToPay, "Email"));
        });

        Button smsBtn = createCheckoutButton("📱   SEND VIA SMS");
        smsBtn.setOnAction(e -> {
            notifDialog.close();
            Platform.runLater(() -> processDatabaseCheckout(paymentMethod, amountToPay, "SMS"));
        });

        content.getChildren().addAll(header, emailBtn, smsBtn);
        notifDialog.getDialogPane().setContent(content);
        notifDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        notifDialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        notifDialog.showAndWait();
    }

    // Helper for creating uniform checkout buttons
    private Button createCheckoutButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(280);
        btn.setPrefHeight(50);
        btn.setStyle("-fx-background-color: #2A2A2A; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-color: #555555; -fx-border-radius: 5; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #3A3A3A; -fx-text-fill: " + COLOR_YELLOW + "; -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-color: " + COLOR_YELLOW + "; -fx-border-radius: 5; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #2A2A2A; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-color: #555555; -fx-border-radius: 5; -fx-cursor: hand;"));
        return btn;
    }

    // ==========================================
    // DATABASE INTEGRATION
    // ==========================================

    private void processDatabaseCheckout(String paymentMethod, double amountToPay, String notificationPref) {
        // Generates a guaranteed unique 8-character alphanumeric ID
        String bookingId = "BKG-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        try (Connection conn = DatabaseManager.getConnection()) {

            // 1. Save Main Booking Details
            String insertBookingQuery = "INSERT INTO bookings (booking_id, payment_method, total_paid, notification_pref) VALUES (?, ?, ?, ?)";
            try (PreparedStatement bookingStmt = conn.prepareStatement(insertBookingQuery)) {
                bookingStmt.setString(1, bookingId);
                bookingStmt.setString(2, paymentMethod);
                bookingStmt.setDouble(3, amountToPay);
                bookingStmt.setString(4, notificationPref);
                bookingStmt.executeUpdate();
            }

            // 2. Save Associated Tickets
            String insertTicketQuery = "INSERT INTO booked_tickets (booking_id, movie_title, price) VALUES (?, ?, ?)";
            try (PreparedStatement ticketStmt = conn.prepareStatement(insertTicketQuery)) {
                for (Movie movie : basket) {
                    ticketStmt.setString(1, bookingId);
                    ticketStmt.setString(2, movie.getTitle());
                    ticketStmt.setDouble(3, movie.getPrice());
                    ticketStmt.executeUpdate();
                }
            }

            // 3. Dispatch Notification to Profile
            String notificationDesc = String.format("Booking %s confirmed! Paid $%.2f via %s. Receipt sent via %s.",
                    bookingId, amountToPay, paymentMethod, notificationPref);
            UserProfile.addNotification("New Booking", notificationDesc);

            // 4. Reset Application State
            basket.clear();
            loadMovies(currentSearchKeyword, currentCityFilter); // Refreshes UI buttons

            // 5. Show Success Popup
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Payment Successful");
            successAlert.setHeaderText("Checkout Complete!");
            successAlert.setContentText("Your Booking ID is: " + bookingId + "\nAll tickets have been saved to the database.");
            successAlert.showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Database Error");
            errorAlert.setHeaderText("Could not save booking!");
            errorAlert.setContentText("Please check your database connection.\nError: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    private void goToTeammateLogin(Stage stage) {
        System.out.println("Switching to teammate's login system...");
    }

    public static void main(String[] args) {
        launch(args);
    }
}





