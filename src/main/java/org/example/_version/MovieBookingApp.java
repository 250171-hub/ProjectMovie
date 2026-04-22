package org.example._version;


import javafx.animation.FadeTransition;
import javafx.application.Application;
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
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import java.util.Map;
import java.util.HashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static javafx.application.Application.launch;

public class MovieBookingApp extends Application {
    private MovieCatalog catalog = new MovieCatalog();
    private FlowPane movieGrid = new FlowPane();
    private List<Movie> basket = new ArrayList<>();

    private Map<Movie, Button> cardButtons = new HashMap<>();


    // --- NEW: Track current page state ---
    private String currentSearch = "";
    private String currentCity = "All Cities";

    // Theme Colors
    private final String BLACK = "#121212";
    private final String YELLOW = "#FFD700";
    private final String DARK_GRAY = "#1E1E1E";

    // Slider variables
    private int currentRecIndex = 0;
    private List<Movie> recommendedMovies;
    private ImageView heroImageView = new ImageView();
    private Label heroTitle = new Label();

    @Override
    public void start(Stage primaryStage) {
        // --- 1. NAVIGATION BAR ---
        HBox navBar = new HBox(20);
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(15, 40, 15, 40));
        navBar.setStyle("-fx-background-color: " + BLACK + "; -fx-border-color: #333333; -fx-border-width: 0 0 1 0;");

        Label logo = new Label("CINEMA RESERVE");
        logo.setFont(Font.font("Verdana", FontWeight.BOLD, 26));
        logo.setTextFill(Color.web(YELLOW));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchBox = new TextField();
        searchBox.setPromptText("Search movies...");
        searchBox.setPrefWidth(200);
        searchBox.setStyle("-fx-background-color: #252525; -fx-text-fill: white; -fx-prompt-text-fill: #888888;");

        ComboBox<String> cityFilter = new ComboBox<>();
// Add "All Cities" to the list
        cityFilter.getItems().setAll("All Cities", "Tashkent", "London", "New York");
// Set "All Cities" as the default
        cityFilter.setValue("All Cities");
        cityFilter.setStyle("-fx-background-color: " + YELLOW + "; -fx-font-weight: bold;");

        cityFilter.getItems().setAll("Tashkent", "London", "New York");
        cityFilter.setValue("Tashkent");
        cityFilter.setStyle("-fx-background-color: " + YELLOW + "; -fx-font-weight: bold;");

        Button loginBtn = new Button("LOG IN");
        loginBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + YELLOW + "; -fx-border-color: " + YELLOW + "; -fx-border-radius: 5; -fx-font-weight: bold; -fx-padding: 5 15;");
        loginBtn.setOnAction(e -> goToTeammateLogin(primaryStage));

        navBar.getChildren().addAll(logo, spacer, searchBox, cityFilter, loginBtn);

        // --- 2. HERO SLIDER (Large Featured Movie) ---
        StackPane heroSlider = new StackPane();
        heroSlider.setPrefHeight(400);
        heroSlider.setStyle("-fx-background-color: black;");

        heroImageView.setFitHeight(400);
        heroImageView.setPreserveRatio(true);

        VBox overlay = new VBox(10);
        overlay.setAlignment(Pos.BOTTOM_LEFT);
        overlay.setPadding(new Insets(0, 0, 40, 60));
        // Gradient overlay for better text readability
        overlay.setStyle("-fx-background-color: linear-gradient(to top, rgba(18,18,18,1), rgba(18,18,18,0));");

        heroTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 48));
        heroTitle.setTextFill(Color.WHITE);

        Button nextBtn = new Button("NEXT FEATURED ❯");
        nextBtn.setStyle("-fx-background-color: " + YELLOW + "; -fx-font-weight: bold; -fx-padding: 10 20;");
        nextBtn.setOnAction(e -> rotateHeroImage());

        overlay.getChildren().addAll(heroTitle, nextBtn);
        heroSlider.getChildren().addAll(heroImageView, overlay);

        // --- 3. MOVIE GRID ---
        movieGrid.setHgap(30);
        movieGrid.setVgap(30);
        movieGrid.setPadding(new Insets(40));
        movieGrid.setStyle("-fx-background-color: " + BLACK + ";");

        ScrollPane mainScroll = new ScrollPane(movieGrid);
        mainScroll.setFitToWidth(true);
        // FIX: Ensure scrollpane background is fully black
        mainScroll.setStyle("-fx-background: " + BLACK + "; -fx-background-color: " + BLACK + "; -fx-border-color: " + BLACK + ";");
        VBox.setVgrow(mainScroll, Priority.ALWAYS);

        // CRITICAL FIX: Bind the grid width to the scrollpane so items wrap properly
        movieGrid.prefWrapLengthProperty().bind(mainScroll.widthProperty().subtract(80));

        // --- 4. FLOATING BASKET BUTTON ---
        Button basketBtn = new Button("🛒");
        basketBtn.setStyle("-fx-background-color: " + YELLOW + "; -fx-background-radius: 50; -fx-font-size: 28; -fx-pref-width: 75; -fx-pref-height: 75; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 15, 0, 0, 0);");
        basketBtn.setOnAction(e -> showBasket());


        // --- LAYOUT ASSEMBLY ---
        StackPane rootStack = new StackPane();
        VBox mainLayout = new VBox(navBar, heroSlider, mainScroll);
        rootStack.getChildren().addAll(mainLayout, basketBtn);
        StackPane.setAlignment(basketBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(basketBtn, new Insets(40));

        // --- INITIAL DATA LOAD ---
        recommendedMovies = catalog.searchAndFilter("", Genre.ALL, "All Cities", 0.0);
        Collections.shuffle(recommendedMovies);
        rotateHeroImage();

        loadMovies(searchBox.getText(), cityFilter.getValue());

        // Listeners
        searchBox.textProperty().addListener((o, old, newVal) -> loadMovies(newVal, cityFilter.getValue()));
        cityFilter.setOnAction(e -> loadMovies(searchBox.getText(), cityFilter.getValue()));

        Scene scene = new Scene(rootStack, 1300, 900);
        primaryStage.setTitle("Cinema Reserve | Premium Online Booking");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private void updateButtonVisuals(Movie m) {
        Button btn = cardButtons.get(m);
        if (btn != null) { // If the button is currently on the screen
            if (basket.contains(m)) {
                btn.setText("ADDED ✓");
                btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12;");
                btn.setDisable(true);
            } else {
                btn.setText("BOOK NOW");
                btn.setStyle("-fx-background-color: " + YELLOW + "; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 12; -fx-cursor: hand;");
                btn.setDisable(false);
            }
        }
    }

    private void rotateHeroImage() {
        if (recommendedMovies == null || recommendedMovies.isEmpty()) return;
        Movie m = recommendedMovies.get(currentRecIndex);

        FadeTransition ftOut = new FadeTransition(Duration.millis(400), heroImageView);
        ftOut.setFromValue(1.0);
        ftOut.setToValue(0.2);
        ftOut.setOnFinished(e -> {
            heroImageView.setImage(new Image(m.getImageUrl(), 1200, 400, true, true));
            heroTitle.setText(m.getTitle().toUpperCase());
            FadeTransition ftIn = new FadeTransition(Duration.millis(400), heroImageView);
            ftIn.setFromValue(0.2);
            ftIn.setToValue(1.0);
            ftIn.play();
        });
        ftOut.play();
        currentRecIndex = (currentRecIndex + 1) % recommendedMovies.size();
    }

    private void loadMovies(String kw, String city) {
        movieGrid.getChildren().clear();
        this.currentSearch = kw;
        this.currentCity = city;
        List<Movie> movies = catalog.searchAndFilter(kw, Genre.ALL, city, 0.0);

        if (movies.isEmpty()) {
            Label noResults = new Label("No movies found in " + city + ". Try searching something else!");
            noResults.setTextFill(Color.GRAY);
            noResults.setFont(Font.font(20));
            movieGrid.getChildren().add(noResults);
        } else {
            for (Movie m : movies) {
                movieGrid.getChildren().add(createMovieCard(m));
            }
        }
    }

    private VBox createMovieCard(Movie m) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: " + DARK_GRAY + "; -fx-background-radius: 15; -fx-border-color: #333333; -fx-border-radius: 15;");
        card.setPadding(new Insets(15));
        card.setPrefWidth(250);
        card.setAlignment(Pos.TOP_LEFT);

        ImageView poster = new ImageView(new Image(m.getImageUrl(), 220, 310, true, true));
        poster.setStyle("-fx-background-radius: 10;");

        Label title = new Label(m.getTitle().toUpperCase());
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 17));
        title.setWrapText(true);
        title.setMinHeight(50);

        Label details = new Label("⭐ " + m.getRating() + " | " + m.getGenre());
        details.setTextFill(Color.web(YELLOW));

        Label times = new Label("🕒 " + m.getTimes());
        times.setTextFill(Color.LIGHTGRAY);
        times.setFont(Font.font(12));

        Label price = new Label("$" + String.format("%.2f", m.getPrice()));
        price.setTextFill(Color.WHITE);
        price.setFont(Font.font("Verdana", FontWeight.BOLD, 20));

        Button bookBtn = new Button("BOOK NOW");
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        bookBtn.setStyle("-fx-background-color: " + YELLOW + "; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 12; -fx-cursor: hand;");

        cardButtons.put(m, bookBtn);
        updateButtonVisuals(m);

        bookBtn.setOnAction(e -> {
            basket.add(m);
            bookBtn.setText("ADDED ✓");
            bookBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12;");
            bookBtn.setDisable(true);
            updateButtonVisuals(m);
        });
        if (basket.contains(m)) {
            bookBtn.setText("ADDED ✓");
            bookBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12;");
            bookBtn.setDisable(true);
        } else {
            bookBtn.setText("BOOK NOW");
            bookBtn.setStyle("-fx-background-color: " + YELLOW + "; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 12; -fx-cursor: hand;");
            bookBtn.setDisable(false);
        }

        bookBtn.setOnAction(e -> {
            basket.add(m);
            bookBtn.setText("ADDED ✓");
            bookBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12;");
            bookBtn.setDisable(true);
        });

        card.getChildren().addAll(poster, title, details, times, price, bookBtn);

        // Hover Effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #252525; -fx-background-radius: 15; -fx-border-color: " + YELLOW + "; -fx-border-radius: 15;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: " + DARK_GRAY + "; -fx-background-radius: 15; -fx-border-color: #333333; -fx-border-radius: 15;"));

        return card;
    }

    private void showBasket() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Your Booked Tickets");

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: " + DARK_GRAY + ";");
        content.setMinWidth(480);

        Label header = new Label("MY RESERVATIONS");
        header.setTextFill(Color.web(YELLOW));
        header.setFont(Font.font("Verdana", FontWeight.BOLD, 22));
        content.getChildren().add(header);

        if (basket.isEmpty()) {
            Label empty = new Label("Your basket is empty.");
            empty.setTextFill(Color.GRAY);
            content.getChildren().add(empty);

            Button close = new Button("CLOSE");
            close.setMaxWidth(Double.MAX_VALUE);
            // FIX 1: Use dialog.close() for a reliable exit
            close.setOnAction(e -> dialog.close());
            close.setStyle("-fx-background-color: " + YELLOW + "; -fx-font-weight: bold;");
            content.getChildren().add(close);
        } else {
            double tempTotal = 0;

            // Loop through the basket
            for (int i = 0; i < basket.size(); i++) {
                Movie m = basket.get(i);
                HBox item = new HBox(10);
                item.setAlignment(Pos.CENTER_LEFT);

                // FIX 2: Add a Remove Button for individual movies
                Button removeBtn = new Button("✖");
                removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #F44336; -fx-font-size: 14px; -fx-cursor: hand;");
                removeBtn.setOnAction(e -> {
                    basket.remove(m);


                    updateButtonVisuals(m);


                    // 1. Remove the specific movie from the list
                    dialog.setResult(null);
                    dialog.close();// 2. Close the current dialog
                    javafx.application.Platform.runLater(() -> {
                        showBasket();
                    });// 3. Re-open it to refresh the totals and list visually!
                });

                Label name = new Label("• " + m.getTitle());
                name.setTextFill(Color.WHITE);
                Region s = new Region();
                HBox.setHgrow(s, Priority.ALWAYS);
                Label p = new Label("$" + String.format("%.2f", m.getPrice()));
                p.setTextFill(Color.LIGHTGRAY);

                // Add the removeBtn to the row
                item.getChildren().addAll(removeBtn, name, s, p);
                content.getChildren().add(item);

                tempTotal += m.getPrice();
            }

            final double initialTotal = tempTotal;
            final double[] finalTotal = {initialTotal};

            Separator sep = new Separator();
            sep.setStyle("-fx-background-color: #333333;");

            // --- COUPON SYSTEM ---
            HBox couponBox = new HBox(10);
            couponBox.setAlignment(Pos.CENTER_LEFT);

            TextField couponField = new TextField();
            couponField.setPromptText("Enter code (e.g., CINEMA20)");
            couponField.setStyle("-fx-background-color: #252525; -fx-text-fill: white; -fx-prompt-text-fill: #888888; -fx-border-color: #333333; -fx-border-radius: 3;");

            Button applyBtn = new Button("APPLY");
            applyBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-font-weight: bold;");

            Label discountMsg = new Label();
            discountMsg.setFont(Font.font(12));

            couponBox.getChildren().addAll(couponField, applyBtn, discountMsg);

            // --- TOTAL LABELS ---
            VBox totalBox = new VBox(5);
            totalBox.setAlignment(Pos.CENTER_RIGHT);
            Label originalTotalLabel = new Label("Initial Price: $" + String.format("%.2f", initialTotal));
            originalTotalLabel.setTextFill(Color.LIGHTGRAY);
            originalTotalLabel.setFont(Font.font("Verdana", 14));

            Label finalTotalLabel = new Label("TOTAL: $" + String.format("%.2f", initialTotal));
            finalTotalLabel.setTextFill(Color.web(YELLOW));
            finalTotalLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));

            totalBox.getChildren().addAll(originalTotalLabel, finalTotalLabel);

            applyBtn.setOnAction(e -> {
                if (couponField.getText().equalsIgnoreCase("CINEMA20")) {
                    double discount = initialTotal * 0.20; // 20% off
                    finalTotal[0] = initialTotal - discount;

                    originalTotalLabel.setText("Initial Price: $" + String.format("%.2f", initialTotal) + " (Strikethrough)");
                    originalTotalLabel.setStyle("-fx-strikethrough: true; -fx-text-fill: gray;");

                    finalTotalLabel.setText("SALE TOTAL: $" + String.format("%.2f", finalTotal[0]));
                    finalTotalLabel.setTextFill(Color.web("#4CAF50")); // Green for success

                    discountMsg.setText("-20% Applied!");
                    discountMsg.setTextFill(Color.web("#4CAF50"));
                    applyBtn.setDisable(true);
                    couponField.setDisable(true);
                } else {
                    discountMsg.setText("Invalid Code");
                    discountMsg.setTextFill(Color.web("#F44336")); // Red for error
                }
            });

            content.getChildren().addAll(sep, couponBox, totalBox);

            // --- CHECKOUT BUTTONS ---
            HBox buttonBox = new HBox(15);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(10, 0, 0, 0));

            Button close = new Button("CANCEL");
            close.setStyle("-fx-background-color: transparent; -fx-text-fill: " + YELLOW + "; -fx-border-color: " + YELLOW + "; -fx-border-radius: 5;");
            // FIX 1: Use dialog.close() for a reliable exit
            close.setOnAction(e -> dialog.close());

            Button checkout = new Button("PROCEED TO CHECKOUT");
            checkout.setStyle("-fx-background-color: " + YELLOW + "; -fx-font-weight: bold; -fx-text-fill: black; -fx-background-radius: 5;");
            checkout.setOnAction(e -> {
                dialog.setResult(null);
                dialog.close();
                showPaymentOptions(finalTotal[0]);
                javafx.application.Platform.runLater(() -> {
                    showPaymentOptions(finalTotal[0]);
                });
            });

            buttonBox.getChildren().addAll(close, checkout);
            content.getChildren().add(buttonBox);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("-fx-background-color: " + DARK_GRAY + ";");

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        dialog.showAndWait();
    }

    // --- UPDATED METHOD: Redesigned Payment Options Dialog ---
    private void showPaymentOptions(double amountToPay) {
        Dialog<Void> payDialog = new Dialog<>();
        payDialog.setTitle("Secure Checkout");

        VBox content = new VBox(20);
        content.setPadding(new Insets(35));
        content.setStyle("-fx-background-color: " + DARK_GRAY + "; -fx-border-color: " + YELLOW + "; -fx-border-width: 2;");
        content.setAlignment(Pos.CENTER);

        Label header = new Label("SELECT PAYMENT METHOD");
        header.setTextFill(Color.WHITE);
        header.setFont(Font.font("Verdana", FontWeight.BOLD, 18));

        Label amountLabel = new Label("Amount Due: $" + String.format("%.2f", amountToPay));
        amountLabel.setTextFill(Color.web(YELLOW));
        amountLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 24));

        // Credit Card Button Design
        Button cardBtn = new Button("💳   PAY WITH CREDIT CARD");
        cardBtn.setPrefWidth(280);
        cardBtn.setPrefHeight(50);
        cardBtn.setStyle("-fx-background-color: #2A2A2A; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-color: #555555; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
        cardBtn.setOnMouseEntered(e -> cardBtn.setStyle("-fx-background-color: #3A3A3A; -fx-text-fill: " + YELLOW + "; -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-color: " + YELLOW + "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;"));
        cardBtn.setOnMouseExited(e -> cardBtn.setStyle("-fx-background-color: #2A2A2A; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-color: #555555; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;"));
        cardBtn.setOnAction(e -> {
            payDialog.setResult(null);
            payDialog.close();
            generateBookingId("Credit Card", amountToPay);
        });

        // Cash Button Design
        Button cashBtn = new Button("💵   PAY AT CINEMA (CASH)");
        cashBtn.setPrefWidth(280);
        cashBtn.setPrefHeight(50);
        cashBtn.setStyle("-fx-background-color: #2A2A2A; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-color: #555555; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
        cashBtn.setOnMouseEntered(e -> cashBtn.setStyle("-fx-background-color: #3A3A3A; -fx-text-fill: " + YELLOW + "; -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-color: " + YELLOW + "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;"));
        cashBtn.setOnMouseExited(e -> cashBtn.setStyle("-fx-background-color: #2A2A2A; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-color: #555555; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;"));
        cashBtn.setOnAction(e -> {
            payDialog.setResult(null);
            generateBookingId("Cash", amountToPay);
        });

        Button cancelBtn = new Button("← Back to Basket");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #888888; -fx-cursor: hand;");
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand;"));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #888888; -fx-cursor: hand;"));
        cancelBtn.setOnAction(e -> {
            payDialog.setResult(null);
            showBasket(); // Re-open basket if they cancel
        });

        content.getChildren().addAll(header, amountLabel, cardBtn, cashBtn, cancelBtn);

        payDialog.getDialogPane().setContent(content);
        payDialog.getDialogPane().setStyle("-fx-background-color: transparent;");
        payDialog.initStyle(javafx.stage.StageStyle.TRANSPARENT); // Removes default OS window borders for a cleaner look

        payDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        payDialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);

        payDialog.showAndWait();
    }

    // --- UPDATED METHOD: Booking Confirmation takes the final price ---
    private void generateBookingId(String paymentMethod, double finalTotal) {
        String bookingId = "BKG-" + (int)(Math.random() * 900000 + 100000);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Confirmation");
        alert.setHeaderText("Transaction Successful!");

        String confirmationMessage = String.format(
                "Payment Method: %s\n" +
                        "Tickets Booked: %d\n" +
                        "Total Paid: $%.2f\n\n" +
                        "Your Booking ID is: %s\n\n" +
                        "Please present this ID at the cinema counter.",
                paymentMethod, basket.size(), finalTotal, bookingId
        );

        alert.setContentText(confirmationMessage);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + DARK_GRAY + "; -fx-border-color: #4CAF50; -fx-border-width: 2;");
        dialogPane.lookupAll(".label").forEach(node -> ((Label)node).setTextFill(Color.WHITE));

        alert.showAndWait();
        basket.clear();
        // Empty basket after successful checkout
        loadMovies(currentSearch, currentCity);
    }

    private void goToTeammateLogin(Stage stage) {
        System.out.println("Switching to teammate's login system...");
        // Add your teammate's scene switching logic here!
    }
    public static void main(String[] args){launch(args);}
}





