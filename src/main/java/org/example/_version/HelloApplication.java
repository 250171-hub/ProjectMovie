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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HelloApplication extends Application {
    private MovieCatalog catalog = new MovieCatalog();
    private FlowPane movieGrid = new FlowPane();
    private List<Movie> basket = new ArrayList<>();

    // Theme Colors
    private final String BLACK = "#121212";
    private final String YELLOW = "#FFD700";
    private final String DARK_GRAY = "#1E1E1E";

    // Slider variables
    private int currentRecIndex = 0;
    private List<Movie> recommendedMovies;
    private ImageView heroImageView = new ImageView();
    private Label heroTitle = new Label();

    private boolean isLoggedIn = false; // Set to true to see the avatar
    private String userAvatarUrl = "https://cdn-icons-png.flaticon.com/512/3135/3135715.png"; // Placeholder avatar

    @Override
    public void start(Stage primaryStage) {
        // --- 1. NAVIGATION BAR ---
        // --- UPDATED NAVIGATION BAR ---
        HBox navBar = new HBox(20);
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(15, 40, 15, 40));
        navBar.setStyle("-fx-background-color: " + BLACK + "; -fx-border-color: #333333; -fx-border-width: 0 0 1 0;");

        Label logo = new Label("CINEMA RESERVE");
        logo.setFont(Font.font("Verdana", FontWeight.BOLD, 26));
        logo.setTextFill(Color.web(YELLOW));

// This spacer pushes everything after it (Search, City, Login) to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchBox = new TextField();
        searchBox.setPromptText("Search movies...");
        searchBox.setStyle("-fx-background-color: #252525; -fx-text-fill: white; -fx-border-radius: 5;");

        ComboBox<String> cityFilter = new ComboBox<>();
        cityFilter.getItems().setAll("Tashkent", "London", "New York");
        cityFilter.setValue("Tashkent");
        cityFilter.setStyle("-fx-background-color: " + YELLOW + "; -fx-font-weight: bold;");

// --- THE LOGIN BUTTON ---
        Button loginBtn = new Button("LOG IN");
        loginBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + YELLOW +
                "; -fx-border-color: " + YELLOW + "; -fx-border-radius: 5; -fx-font-weight: bold; -fx-padding: 5 15;");

// Action to trigger teammate's code
       // loginBtn.setOnAction(e -> goToTeammateLogin(primaryStage));

        navBar.getChildren().addAll(logo, spacer, searchBox, cityFilter, loginBtn);

        // --- 2. HERO SLIDER (Large Movie Recommendation) ---
        StackPane heroSlider = new StackPane();
        heroSlider.setPrefHeight(400);
        heroSlider.setStyle("-fx-background-color: black;");

        heroImageView.setFitHeight(400);
        heroImageView.setPreserveRatio(true);

        // Dark Overlay for text readability
        VBox overlay = new VBox(10);
        overlay.setAlignment(Pos.BOTTOM_LEFT);
        overlay.setPadding(new Insets(0, 0, 40, 60));
        overlay.setStyle("-fx-background-color: linear-gradient(to top, rgba(18,18,18,1), rgba(18,18,18,0));");

        heroTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 48));
        heroTitle.setTextFill(Color.WHITE);

        Button nextBtn = new Button("NEXT FEATURED ❯");
        nextBtn.setStyle("-fx-background-color: " + YELLOW + "; -fx-font-weight: bold;");
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
        // Fix for the "White Background" - set all scrollpane parts to black
        mainScroll.setStyle("-fx-background: " + BLACK + "; -fx-background-color: " + BLACK + "; -fx-border-color: " + BLACK + ";");
        VBox.setVgrow(mainScroll, Priority.ALWAYS);

        // --- 4. FLOATING BASKET BUTTON ---
        Button basketBtn = new Button("🛒");
        basketBtn.setStyle("-fx-background-color: " + YELLOW + "; -fx-background-radius: 50; -fx-font-size: 24; -fx-pref-width: 70; -fx-pref-height: 70; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10, 0, 0, 0);");
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
        rotateHeroImage(); // Show first movie

        loadMovies(searchBox.getText(), cityFilter.getValue());

        // Listeners
        searchBox.textProperty().addListener((o, old, newVal) -> loadMovies(newVal, cityFilter.getValue()));
        cityFilter.setOnAction(e -> loadMovies(searchBox.getText(), cityFilter.getValue()));

        Scene scene = new Scene(rootStack, 1300, 900);
        primaryStage.setTitle("Cinema Reserve | Premium");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void rotateHeroImage() {
        if (recommendedMovies.isEmpty()) return;

        Movie m = recommendedMovies.get(currentRecIndex);

        // Smooth Fade Out/In Transition
        FadeTransition ft = new FadeTransition(Duration.millis(500), heroImageView);
        ft.setFromValue(1.0);
        ft.setToValue(0.2);
        ft.setOnFinished(e -> {
            heroImageView.setImage(new Image(m.getImageUrl(), 1200, 400, true, true));
            heroTitle.setText(m.getTitle().toUpperCase());
            FadeTransition ftIn = new FadeTransition(Duration.millis(500), heroImageView);
            ftIn.setFromValue(0.2);
            ftIn.setToValue(1.0);
            ftIn.play();
        });
        ft.play();

        currentRecIndex = (currentRecIndex + 1) % recommendedMovies.size();
    }

    private void loadMovies(String kw, String city) {
        movieGrid.getChildren().clear();
        List<Movie> movies = catalog.searchAndFilter(kw, Genre.ALL, city, 0.0);
        for (Movie m : movies) {
            movieGrid.getChildren().add(createMovieCard(m));
        }
    }

    private VBox createMovieCard(Movie m) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: " + DARK_GRAY + "; -fx-background-radius: 15; -fx-border-color: #333333; -fx-border-radius: 15;");
        card.setPadding(new Insets(15));
        card.setPrefWidth(240);
        card.setAlignment(Pos.TOP_LEFT);

        ImageView poster = new ImageView(new Image(m.getImageUrl(), 210, 300, true, true));
        poster.setStyle("-fx-background-radius: 10;");

        Label title = new Label(m.getTitle().toUpperCase());
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        title.setWrapText(true);

        Label details = new Label("⭐ " + m.getRating() + " | " + m.getGenre());
        details.setTextFill(Color.web(YELLOW));

        Label times = new Label("🕒 " + m.getTimes());
        times.setTextFill(Color.LIGHTGRAY);
        times.setFont(Font.font(12));

        Label price = new Label("$" + String.format("%.2f", m.getPrice()));
        price.setTextFill(Color.WHITE);
        price.setFont(Font.font("Verdana", FontWeight.BOLD, 18));

        Button bookBtn = new Button("BOOK NOW");
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        bookBtn.setStyle("-fx-background-color: " + YELLOW + "; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 10;");
        bookBtn.setOnAction(e -> {
            basket.add(m);
            // Non-blocking confirmation
            bookBtn.setText("ADDED ✓");
            bookBtn.setDisable(true);
        });

        card.getChildren().addAll(poster, title, details, times, price, bookBtn);
        return card;
    }

    private void showBasket() {
        // Create a custom styled dialog for the basket
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Your Booked Tickets");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: " + DARK_GRAY + ";");
        content.setMinWidth(400);

        Label header = new Label("MY RESERVATIONS");
        header.setTextFill(Color.web(YELLOW));
        header.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        content.getChildren().add(header);

        double total = 0;
        for (Movie m : basket) {
            HBox item = new HBox(10);
            Label name = new Label(m.getTitle());
            name.setTextFill(Color.WHITE);
            Region s = new Region();
            HBox.setHgrow(s, Priority.ALWAYS);
            Label p = new Label("$" + m.getPrice());
            p.setTextFill(Color.LIGHTGRAY);
            item.getChildren().addAll(name, s, p);
            content.getChildren().add(item);
            total += m.getPrice();
        }

        Separator sep = new Separator();
        Label totalLabel = new Label("TOTAL: $" + String.format("%.2f", total));
        totalLabel.setTextFill(Color.WHITE);
        totalLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18));

        Button close = new Button("CLOSE");
        close.setOnAction(e -> dialog.setResult(null));
        close.setStyle("-fx-background-color: " + YELLOW + ";");

        content.getChildren().addAll(sep, totalLabel, close);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("-fx-background-color: " + DARK_GRAY + ";");
        dialog.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}
