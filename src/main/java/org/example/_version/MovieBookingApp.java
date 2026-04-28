package org.example._version;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovieBookingApp extends Application {

    // --- STATE MANAGEMENT ---
    private final MovieCatalog catalog = new MovieCatalog();
    private final List<Movie> basket = new ArrayList<>();
    private final Map<Movie, Button> cardButtons = new HashMap<>();

    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();
    private final ExecutorService imageLoader = Executors.newFixedThreadPool(4);

    // --- FILTER STATE ---
    private String currentSearchKeyword  = "";
    private String currentCityFilter     = "All Cities";
    private String currentGenreFilter    = "All Genres";
    private String currentYearFilter     = "All Years";
    private String currentLanguageFilter = "All Languages";

    // --- HERO SLIDER STATE ---
    private int currentHeroImageIndex = 0;
    private List<Movie> recommendedMovies;

    // --- UI COMPONENTS ---
    private final FlowPane  movieGrid     = new FlowPane();
    private final ImageView heroImageView = new ImageView();
    private final Label     heroTitle     = new Label();
    private final Image userAvatarImage   = new Image(
            "https://cdn-icons-png.flaticon.com/512/149/149071.png", 150, 150, true, true);
    private final ImageView navAvatarIcon = new ImageView(userAvatarImage);

    // --- THEME CONSTANTS ---
    private static final String COLOR_BLACK         = "#121212";
    private static final String COLOR_YELLOW        = "#FFD700";
    private static final String COLOR_DARK_GRAY     = "#1E1E1E";
    private static final String COLOR_SUCCESS_GREEN = "#4CAF50";
    private static final String COLOR_ERROR_RED     = "#F44336";
    private static final String PLACEHOLDER_URL     =
            "https://via.placeholder.com/220x310/1E1E1E/FFD700?text=Loading...";

    // --- Shared debounce timer (used by nav search + filter bar) ---
    private PauseTransition filterDebounce;

    // =========================================================================
    // APPLICATION ENTRY POINT
    // =========================================================================

    @Override
    public void start(Stage primaryStage) {
        UserSession.getInstance().loginUser(1, "JohnDoe", 100.0, "", "", "", "");

        HBox       navBar     = createNavigationBar(primaryStage);
        StackPane  heroSlider = createHeroSlider();
        ScrollPane mainScroll = createMainMovieGrid();
        Button     basketBtn  = createBasketButton();

        StackPane rootStack = new StackPane();
        VBox mainLayout = new VBox(navBar, heroSlider, mainScroll);
        rootStack.getChildren().addAll(mainLayout, basketBtn);
        StackPane.setAlignment(basketBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(basketBtn, new Insets(40));

        initializeDataAsync();

        Scene scene = new Scene(rootStack, 1300, 900);
        primaryStage.setTitle("Cinema Reserve | Premium Online Booking");
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> imageLoader.shutdownNow());
    }

    // =========================================================================
    // NAV BAR  —  city filter removed, Shop button added
    // =========================================================================

    private HBox createNavigationBar(Stage primaryStage) {
        HBox navBar = new HBox(20);
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(15, 40, 15, 40));
        navBar.setStyle("-fx-background-color: " + COLOR_BLACK +
                "; -fx-border-color: #333333; -fx-border-width: 0 0 1 0;");

        // Logo
        Label logo = new Label("CINEMA RESERVE");
        logo.setFont(Font.font("Verdana", FontWeight.BOLD, 26));
        logo.setTextFill(Color.web(COLOR_YELLOW));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search box
        TextField searchBox = new TextField();
        searchBox.setPromptText("Search movies...");
        searchBox.setPrefWidth(220);
        searchBox.setStyle("-fx-background-color: #252525; -fx-text-fill: white;" +
                " -fx-prompt-text-fill: #888888;" +
                " -fx-border-radius: 5; -fx-background-radius: 5;");

        // Debounce timer — shared with the filter bar below
        PauseTransition searchPause = new PauseTransition(Duration.millis(400));
        searchPause.setOnFinished(e -> loadMoviesAsync(
                searchBox.getText(), currentCityFilter,
                currentGenreFilter, currentYearFilter, currentLanguageFilter));
        searchBox.textProperty().addListener((obs, o, n) -> searchPause.playFromStart());
        this.filterDebounce = searchPause;   // filter bar will reuse this

        // ── SHOP BUTTON ──────────────────────────────────────────────────────
        // Hides this stage and opens the teammate's stage.
        // When the shop window is closed, this stage becomes visible again.
        Button shopBtn = new Button("🛍  SHOP");
        shopBtn.setStyle(
                "-fx-background-color: " + COLOR_YELLOW +
                        "; -fx-text-fill: black; -fx-font-weight: bold;" +
                        " -fx-border-radius: 5; -fx-background-radius: 5;" +
                        " -fx-padding: 6 18; -fx-cursor: hand;");
        shopBtn.setOnMouseEntered(e -> shopBtn.setStyle(
                "-fx-background-color: #e6c200; -fx-text-fill: black; -fx-font-weight: bold;" +
                        " -fx-border-radius: 5; -fx-background-radius: 5;" +
                        " -fx-padding: 6 18; -fx-cursor: hand;"));
        shopBtn.setOnMouseExited(e -> shopBtn.setStyle(
                "-fx-background-color: " + COLOR_YELLOW +
                        "; -fx-text-fill: black; -fx-font-weight: bold;" +
                        " -fx-border-radius: 5; -fx-background-radius: 5;" +
                        " -fx-padding: 6 18; -fx-cursor: hand;"));

        shopBtn.setOnAction(e -> {
            try {
                Stage shopStage = new Stage();

                // ↓↓ REPLACE THIS LINE with your teammate's actual class ↓↓
                // new TeammateMainClass().start(shopStage);

                shopStage.setTitle("Shop");
                shopStage.show();

                // Bring our window back when the shop closes
                shopStage.setOnCloseRequest(ev -> primaryStage.show());

                // Hide our window while shop is open
                primaryStage.hide();
            } catch (Exception ex) {
                System.err.println("Could not open Shop: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // ── PROFILE / LOGIN ───────────────────────────────────────────────────
        navAvatarIcon.setFitWidth(40);
        navAvatarIcon.setFitHeight(40);
        navAvatarIcon.setClip(new javafx.scene.shape.Circle(20, 20, 20));

        Button profileBtn = new Button();
        profileBtn.setGraphic(navAvatarIcon);
        profileBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0;");
        profileBtn.setOnAction(e -> UserProfile.display(primaryStage, navAvatarIcon));

        Button loginBtn = new Button("LOG IN");
        loginBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + COLOR_YELLOW +
                "; -fx-border-color: " + COLOR_YELLOW +
                "; -fx-border-radius: 5; -fx-font-weight: bold; -fx-padding: 5 15;");
        loginBtn.setOnAction(e -> goToTeammateLogin(primaryStage));

        boolean isLoggedIn = UserSession.getInstance().getUserId() > 0;
        loginBtn.setVisible(!isLoggedIn);
        loginBtn.setManaged(!isLoggedIn);
        profileBtn.setVisible(isLoggedIn);
        profileBtn.setManaged(isLoggedIn);

        // Order: Logo | spacer | Search | Shop | Login/Profile
        navBar.getChildren().addAll(logo, spacer, searchBox, shopBtn, loginBtn, profileBtn);
        return navBar;
    }

    // =========================================================================
    // FILTER BAR  —  City | Genre | Year | Language | Reset
    // City was previously in the nav bar; it now lives here with the other filters
    // =========================================================================

    private HBox createFilterBar() {
        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 40, 10, 40));
        bar.setStyle("-fx-background-color: #1A1A1A;" +
                " -fx-border-color: #2A2A2A; -fx-border-width: 0 0 1 0;");

        Label filterLabel = new Label("FILTER:");
        filterLabel.setTextFill(Color.web(COLOR_YELLOW));
        filterLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 12));

        // City (moved from nav bar)
        ComboBox<String> cityBox = new ComboBox<>();
        cityBox.getItems().setAll("All Cities", "Tashkent", "London", "New York");
        cityBox.setValue("All Cities");
        styleFilterCombo(cityBox, "City");
        cityBox.setOnAction(e -> {
            currentCityFilter = cityBox.getValue();
            filterDebounce.playFromStart();
        });

        // Genre
        ComboBox<String> genreBox = new ComboBox<>();
        genreBox.getItems().setAll(
                "All Genres", "Action", "Comedy", "Drama", "Horror",
                "Sci-Fi", "Romance", "Thriller", "Animation", "Documentary");
        genreBox.setValue("All Genres");
        styleFilterCombo(genreBox, "Genre");
        genreBox.setOnAction(e -> {
            currentGenreFilter = genreBox.getValue();
            filterDebounce.playFromStart();
        });

        // Release Year
        ComboBox<String> yearBox = new ComboBox<>();
        List<String> years = new ArrayList<>();
        years.add("All Years");
        for (int y = 2025; y >= 2000; y--) years.add(String.valueOf(y));
        yearBox.getItems().setAll(years);
        yearBox.setValue("All Years");
        styleFilterCombo(yearBox, "Year");
        yearBox.setOnAction(e -> {
            currentYearFilter = yearBox.getValue();
            filterDebounce.playFromStart();
        });

        // Language
        ComboBox<String> langBox = new ComboBox<>();
        langBox.getItems().setAll(
                "All Languages", "English", "Uzbek", "Russian",
                "Spanish", "French", "Korean", "Japanese");
        langBox.setValue("All Languages");
        styleFilterCombo(langBox, "Language");
        langBox.setOnAction(e -> {
            currentLanguageFilter = langBox.getValue();
            filterDebounce.playFromStart();
        });

        // Reset — clears all four filters at once
        Button resetBtn = new Button("✕ RESET");
        resetBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #888888;" +
                " -fx-border-color: #555555; -fx-border-radius: 4; -fx-font-size: 11px;" +
                " -fx-cursor: hand; -fx-padding: 4 10;");
        resetBtn.setOnMouseEntered(e -> resetBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: " + COLOR_ERROR_RED +
                        "; -fx-border-color: " + COLOR_ERROR_RED +
                        "; -fx-border-radius: 4; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 4 10;"));
        resetBtn.setOnMouseExited(e -> resetBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #888888;" +
                        " -fx-border-color: #555555; -fx-border-radius: 4; -fx-font-size: 11px;" +
                        " -fx-cursor: hand; -fx-padding: 4 10;"));
        resetBtn.setOnAction(e -> {
            cityBox.setValue("All Cities");
            genreBox.setValue("All Genres");
            yearBox.setValue("All Years");
            langBox.setValue("All Languages");
            currentCityFilter     = "All Cities";
            currentGenreFilter    = "All Genres";
            currentYearFilter     = "All Years";
            currentLanguageFilter = "All Languages";
            filterDebounce.playFromStart();
        });

        bar.getChildren().addAll(filterLabel, cityBox, genreBox, yearBox, langBox, resetBtn);
        return bar;
    }

    private void styleFilterCombo(ComboBox<String> box, String prompt) {
        box.setPromptText(prompt);
        box.setStyle("-fx-background-color: #252525; -fx-text-fill: white;" +
                " -fx-border-color: #444444; -fx-border-radius: 4;");
        box.setPrefWidth(140);
    }

    // =========================================================================
    // HERO SLIDER
    // =========================================================================

    private StackPane createHeroSlider() {
        StackPane heroSlider = new StackPane();
        heroSlider.setPrefHeight(400);
        heroSlider.setStyle("-fx-background-color: black;");

        heroImageView.setFitHeight(400);
        heroImageView.setPreserveRatio(true);

        VBox overlay = new VBox(10);
        overlay.setAlignment(Pos.BOTTOM_LEFT);
        overlay.setPadding(new Insets(0, 0, 40, 60));
        overlay.setStyle("-fx-background-color: linear-gradient(to top," +
                " rgba(18,18,18,1), rgba(18,18,18,0));");

        heroTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 48));
        heroTitle.setTextFill(Color.WHITE);

        Button nextBtn = new Button("NEXT FEATURED ❯");
        nextBtn.setStyle("-fx-background-color: " + COLOR_YELLOW +
                "; -fx-font-weight: bold; -fx-padding: 10 20;");
        nextBtn.setOnAction(e -> rotateHeroImageAsync());

        overlay.getChildren().addAll(heroTitle, nextBtn);
        heroSlider.getChildren().addAll(heroImageView, overlay);
        return heroSlider;
    }

    // =========================================================================
    // MAIN MOVIE GRID
    // =========================================================================

    private ScrollPane createMainMovieGrid() {
        movieGrid.setHgap(30);
        movieGrid.setVgap(30);
        movieGrid.setPadding(new Insets(40));
        movieGrid.setStyle("-fx-background-color: " + COLOR_BLACK + ";");

        HBox filterBar   = createFilterBar();
        VBox gridWrapper = new VBox(filterBar, movieGrid);
        gridWrapper.setStyle("-fx-background-color: " + COLOR_BLACK + ";");

        ScrollPane mainScroll = new ScrollPane(gridWrapper);
        mainScroll.setFitToWidth(true);
        mainScroll.setStyle("-fx-background: " + COLOR_BLACK +
                "; -fx-background-color: " + COLOR_BLACK +
                "; -fx-border-color: " + COLOR_BLACK + ";");
        VBox.setVgrow(mainScroll, Priority.ALWAYS);

        movieGrid.prefWrapLengthProperty().bind(mainScroll.widthProperty().subtract(80));
        return mainScroll;
    }

    private Button createBasketButton() {
        Button basketBtn = new Button("🛒");
        basketBtn.setStyle("-fx-background-color: " + COLOR_YELLOW +
                "; -fx-background-radius: 50; -fx-font-size: 28;" +
                " -fx-pref-width: 75; -fx-pref-height: 75;" +
                " -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 15, 0, 0, 0);");
        basketBtn.setOnAction(e -> showBasket());
        return basketBtn;
    }

    // =========================================================================
    // ASYNC DATA LOADING
    // =========================================================================

    private void initializeDataAsync() {
        Task<List<Movie>> task = new Task<>() {
            @Override protected List<Movie> call() {
                List<Movie> movies = catalog.searchAndFilter("", Genre.ALL, "All Cities", 0.0);
                Collections.shuffle(movies);
                return movies;
            }
        };
        task.setOnSucceeded(e -> {
            recommendedMovies = task.getValue();
            rotateHeroImageAsync();
            loadMoviesAsync(currentSearchKeyword, currentCityFilter,
                    currentGenreFilter, currentYearFilter, currentLanguageFilter);
        });
        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            Platform.runLater(() ->
                    new Alert(Alert.AlertType.ERROR, "Failed to load movies:\n" +
                            task.getException().getMessage()).showAndWait()
            );
        });
        new Thread(task, "init-data-thread").start();
    }

    private void loadMoviesAsync(String keyword, String city,
                                 String genre, String year, String language) {
        this.currentSearchKeyword  = keyword;
        this.currentCityFilter     = city;
        this.currentGenreFilter    = genre;
        this.currentYearFilter     = year;
        this.currentLanguageFilter = language;

        movieGrid.getChildren().clear();
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setStyle("-fx-progress-color: " + COLOR_YELLOW + ";");
        movieGrid.getChildren().add(spinner);

        Task<List<Movie>> task = new Task<>() {
            @Override protected List<Movie> call() {
                Genre genreEnum = parseGenre(genre);
                int   yearInt   = "All Years".equals(year) ? 0 : Integer.parseInt(year);

                List<Movie> results = catalog.searchAndFilter(keyword, genreEnum, city, 0.0);
                results.removeIf(m -> {
                    boolean failsYear = yearInt > 0 && m.getReleaseYear() != yearInt;
                    boolean failsLang = !"All Languages".equals(language)
                            && !language.equalsIgnoreCase(m.getLanguage());
                    return failsYear || failsLang;
                });
                return results;
            }
        };
        task.setOnSucceeded(e -> {
            movieGrid.getChildren().clear();
            List<Movie> filtered = task.getValue();
            if (filtered.isEmpty()) {
                Label noResults = new Label("No movies found. Try adjusting your filters.");
                noResults.setTextFill(Color.GRAY);
                noResults.setFont(Font.font(20));
                movieGrid.getChildren().add(noResults);
            } else {
                for (Movie movie : filtered)
                    movieGrid.getChildren().add(createMovieCard(movie));
            }
        });
        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            Platform.runLater(() -> {
                movieGrid.getChildren().clear();
                Label err = new Label("Error loading movies: " + task.getException().getMessage());
                err.setTextFill(Color.RED);
                movieGrid.getChildren().add(err);
            });
        });
        new Thread(task, "filter-thread").start();
    }

    private void rotateHeroImageAsync() {
        if (recommendedMovies == null || recommendedMovies.isEmpty()) return;

        Movie movie = recommendedMovies.get(currentHeroImageIndex);
        currentHeroImageIndex = (currentHeroImageIndex + 1) % recommendedMovies.size();

        imageLoader.submit(() -> {
            Image img = getCachedImage(movie.getImageUrl(), 1200, 400);
            Platform.runLater(() -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(400), heroImageView);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.2);
                fadeOut.setOnFinished(ev -> {
                    heroImageView.setImage(img);
                    heroTitle.setText(movie.getTitle().toUpperCase());
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(400), heroImageView);
                    fadeIn.setFromValue(0.2);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });
                fadeOut.play();
            });
        });
    }

    // =========================================================================
    // MOVIE CARD
    // =========================================================================

    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: " + COLOR_DARK_GRAY +
                "; -fx-background-radius: 15; -fx-border-color: #333333; -fx-border-radius: 15;");
        card.setPadding(new Insets(15));
        card.setPrefWidth(250);
        card.setAlignment(Pos.TOP_LEFT);

        ImageView poster = new ImageView();
        poster.setFitWidth(220);
        poster.setFitHeight(310);
        poster.setPreserveRatio(true);
        poster.setImage(getCachedImage(PLACEHOLDER_URL, 220, 310));
        imageLoader.submit(() -> {
            Image real = getCachedImage(movie.getImageUrl(), 220, 310);
            Platform.runLater(() -> poster.setImage(real));
        });

        Label title = new Label(movie.getTitle().toUpperCase());
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 17));
        title.setWrapText(true);
        title.setMinHeight(50);

        Label details = new Label("⭐ " + movie.getRating() + " | " + movie.getGenre());
        details.setTextFill(Color.web(COLOR_YELLOW));

        Label meta = new Label(movie.getReleaseYear() + "  •  " + movie.getLanguage());
        meta.setTextFill(Color.web("#AAAAAA"));
        meta.setFont(Font.font(11));

        Label times = new Label("🕒 " + movie.getTimes());
        times.setTextFill(Color.LIGHTGRAY);
        times.setFont(Font.font(12));

        Label priceLabel = new Label("$" + String.format("%.2f", movie.getPrice()));
        priceLabel.setTextFill(Color.WHITE);
        priceLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));

        Button bookBtn = new Button("BOOK NOW");
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        cardButtons.put(movie, bookBtn);
        updateButtonVisuals(movie);

        bookBtn.setOnAction(e -> {
            if (!basket.contains(movie)) {
                basket.add(movie);
                updateButtonVisuals(movie);
            }
//            // Open cinema → seat selection in a modal window
            Stage owner = (Stage) bookBtn.getScene().getWindow();
            CinemaBookingBridge.open(movie, owner);
        });

        card.getChildren().addAll(poster, title, details, meta, times, priceLabel, bookBtn);
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #252525; -fx-background-radius: 15;" +
                        " -fx-border-color: " + COLOR_YELLOW + "; -fx-border-radius: 15;"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: " + COLOR_DARK_GRAY +
                        "; -fx-background-radius: 15; -fx-border-color: #333333; -fx-border-radius: 15;"));
        return card;
    }

    private void updateButtonVisuals(Movie movie) {
        Button btn = cardButtons.get(movie);
        if (btn == null) return;
        if (basket.contains(movie)) {
            btn.setText("ADDED ✓");
            btn.setStyle("-fx-background-color: " + COLOR_SUCCESS_GREEN +
                    "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12;");
            btn.setDisable(true);
        } else {
            btn.setText("BOOK NOW");
            btn.setStyle("-fx-background-color: " + COLOR_YELLOW +
                    "; -fx-text-fill: black; -fx-font-weight: bold;" +
                    " -fx-padding: 12; -fx-cursor: hand;");
            btn.setDisable(false);
        }
    }

    // =========================================================================
    // IMAGE CACHE
    // =========================================================================

    private Image getCachedImage(String url, double w, double h) {
        return imageCache.computeIfAbsent(url, key -> new Image(key, w, h, true, true));
    }

    // =========================================================================
    // FILTER HELPER
    // =========================================================================

    private Genre parseGenre(String label) {
        if ("All Genres".equals(label)) return Genre.ALL;
        try {
            return Genre.valueOf(label.toUpperCase().replace("-", "_").replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return Genre.ALL;
        }
    }

    // =========================================================================
    // CHECKOUT & DIALOGS
    // =========================================================================

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
        final double[]  finalTotalToPay   = {0.0};

        if (basket.isEmpty()) {
            Label emptyLabel = new Label("Your basket is empty.");
            emptyLabel.setTextFill(Color.GRAY);
            Button closeBtn = new Button("CLOSE");
            closeBtn.setMaxWidth(Double.MAX_VALUE);
            closeBtn.setStyle("-fx-background-color: " + COLOR_YELLOW + "; -fx-font-weight: bold;");
            closeBtn.setOnAction(e -> { dialog.setResult(null); dialog.close(); });
            content.getChildren().addAll(emptyLabel, closeBtn);
        } else {
            double tempTotal = 0;
            for (Movie movie : basket) {
                HBox itemRow = new HBox(10);
                itemRow.setAlignment(Pos.CENTER_LEFT);
                Button removeBtn = new Button("✖");
                removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " +
                        COLOR_ERROR_RED + "; -fx-font-size: 14px; -fx-cursor: hand;");
                removeBtn.setOnAction(e -> {
                    basket.remove(movie);
                    updateButtonVisuals(movie);
                    dialog.setResult(null);
                    dialog.close();
                    Platform.runLater(this::showBasket);
                });
                Label movieName  = new Label("• " + movie.getTitle());
                movieName.setTextFill(Color.WHITE);
                Region spacer    = new Region();
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

            HBox couponBox = new HBox(10);
            couponBox.setAlignment(Pos.CENTER_LEFT);
            TextField couponField = new TextField();
            couponField.setPromptText("Enter code (e.g., CINEMA20)");
            couponField.setStyle("-fx-background-color: #252525; -fx-text-fill: white;" +
                    " -fx-border-color: #333333; -fx-border-radius: 3;");
            Button applyBtn = new Button("APPLY");
            applyBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-font-weight: bold;");
            Label discountMsg = new Label();
            discountMsg.setFont(Font.font(12));
            couponBox.getChildren().addAll(couponField, applyBtn, discountMsg);

            VBox  totalBox           = new VBox(5);
            totalBox.setAlignment(Pos.CENTER_RIGHT);
            Label originalTotalLabel = new Label("Initial Price: $" + String.format("%.2f", initialTotal));
            originalTotalLabel.setTextFill(Color.LIGHTGRAY);
            originalTotalLabel.setFont(Font.font("Verdana", 14));
            Label finalTotalLabel    = new Label("TOTAL: $" + String.format("%.2f", initialTotal));
            finalTotalLabel.setTextFill(Color.web(COLOR_YELLOW));
            finalTotalLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
            totalBox.getChildren().addAll(originalTotalLabel, finalTotalLabel);

            applyBtn.setOnAction(e -> {
                String code = couponField.getText().trim();
                if (code.isEmpty()) {
                    discountMsg.setText("Please enter a code.");
                    discountMsg.setTextFill(Color.web(COLOR_ERROR_RED));
                    return;
                }
                double discountPercent = CouponService.getDiscountPercentage(code);
                if (discountPercent > 0) {
                    double discountAmount = initialTotal * (discountPercent / 100.0);
                    finalTotalToPay[0] = initialTotal - discountAmount;
                    originalTotalLabel.setStyle("-fx-strikethrough: true; -fx-text-fill: gray;");
                    finalTotalLabel.setText("SALE TOTAL: $" + String.format("%.2f", finalTotalToPay[0]));
                    finalTotalLabel.setTextFill(Color.web(COLOR_SUCCESS_GREEN));
                    discountMsg.setText("-" + discountPercent + "% Applied!");
                    discountMsg.setTextFill(Color.web(COLOR_SUCCESS_GREEN));
                    applyBtn.setDisable(true);
                    couponField.setDisable(true);
                } else {
                    discountMsg.setText("Invalid or Expired Code");
                    discountMsg.setTextFill(Color.web(COLOR_ERROR_RED));
                }
            });

            content.getChildren().addAll(separator, couponBox, totalBox);

            HBox buttonBox = new HBox(15);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(10, 0, 0, 0));
            Button cancelBtn = new Button("CANCEL");
            cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " +
                    COLOR_YELLOW + "; -fx-border-color: " + COLOR_YELLOW + "; -fx-border-radius: 5;");
            cancelBtn.setOnAction(e -> { dialog.setResult(null); dialog.close(); });
            Button checkoutBtn = new Button("PROCEED TO CHECKOUT");
            checkoutBtn.setStyle("-fx-background-color: " + COLOR_YELLOW +
                    "; -fx-font-weight: bold; -fx-text-fill: black; -fx-background-radius: 5;");
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

        if (proceedToCheckout[0]) showPaymentOptions(finalTotalToPay[0]);
    }

    private void showPaymentOptions(double amountToPay) {
        Dialog<Void> payDialog = new Dialog<>();
        payDialog.setTitle("Secure Checkout");
        VBox content = new VBox(20);
        content.setPadding(new Insets(35));
        content.setStyle("-fx-background-color: " + COLOR_DARK_GRAY +
                "; -fx-border-color: " + COLOR_YELLOW + "; -fx-border-width: 2;");
        content.setAlignment(Pos.CENTER);
        Label header = new Label("SELECT PAYMENT METHOD");
        header.setTextFill(Color.WHITE);
        header.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        Label amountLabel = new Label("Amount Due: $" + String.format("%.2f", amountToPay));
        amountLabel.setTextFill(Color.web(COLOR_YELLOW));
        amountLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
        Button cardBtn = createCheckoutButton("💳   PAY WITH CREDIT CARD");
        cardBtn.setOnAction(e -> { payDialog.close(); Platform.runLater(() -> showNotificationOptions("Credit Card", amountToPay)); });
        Button cashBtn = createCheckoutButton("💵   PAY AT CINEMA (CASH)");
        cashBtn.setOnAction(e -> { payDialog.close(); Platform.runLater(() -> showNotificationOptions("Cash", amountToPay)); });
        Button cancelBtn = new Button("← Back to Basket");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #888888; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> { payDialog.close(); Platform.runLater(this::showBasket); });
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
        content.setStyle("-fx-background-color: " + COLOR_DARK_GRAY +
                "; -fx-border-color: " + COLOR_YELLOW + "; -fx-border-width: 2;");
        content.setAlignment(Pos.CENTER);
        Label header = new Label("HOW SHOULD WE SEND YOUR RECEIPT?");
        header.setTextFill(Color.WHITE);
        header.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        Button emailBtn = createCheckoutButton("📧   SEND VIA EMAIL");
        emailBtn.setOnAction(e -> { notifDialog.close(); Platform.runLater(() -> processDatabaseCheckout(paymentMethod, amountToPay, "Email")); });
        Button smsBtn = createCheckoutButton("📱   SEND VIA SMS");
        smsBtn.setOnAction(e -> { notifDialog.close(); Platform.runLater(() -> processDatabaseCheckout(paymentMethod, amountToPay, "SMS")); });
        content.getChildren().addAll(header, emailBtn, smsBtn);
        notifDialog.getDialogPane().setContent(content);
        notifDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        notifDialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        notifDialog.showAndWait();
    }

    private Button createCheckoutButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(280);
        btn.setPrefHeight(50);
        btn.setStyle("-fx-background-color: #2A2A2A; -fx-text-fill: white; -fx-font-size: 14px;" +
                " -fx-font-weight: bold; -fx-border-color: #555555; -fx-border-radius: 5; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #3A3A3A; -fx-text-fill: " +
                COLOR_YELLOW + "; -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-color: " +
                COLOR_YELLOW + "; -fx-border-radius: 5; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #2A2A2A; -fx-text-fill: white;" +
                " -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-color: #555555;" +
                " -fx-border-radius: 5; -fx-cursor: hand;"));
        return btn;
    }

    // =========================================================================
    // DATABASE
    // =========================================================================

    private void processDatabaseCheckout(String paymentMethod, double amountToPay, String notificationPref) {
        String bookingId = "BKG-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement s = conn.prepareStatement(
                    "INSERT INTO bookings (booking_id, payment_method, total_paid, notification_pref) VALUES (?, ?, ?, ?)")) {
                s.setString(1, bookingId); s.setString(2, paymentMethod);
                s.setDouble(3, amountToPay); s.setString(4, notificationPref);
                s.executeUpdate();
            }
            try (PreparedStatement s = conn.prepareStatement(
                    "INSERT INTO booked_tickets (booking_id, movie_title, price) VALUES (?, ?, ?)")) {
                for (Movie movie : basket) {
                    s.setString(1, bookingId); s.setString(2, movie.getTitle()); s.setDouble(3, movie.getPrice());
                    s.executeUpdate();
                }
            }
            UserProfile.addNotification("New Booking", String.format(
                    "Booking %s confirmed! Paid $%.2f via %s. Receipt sent via %s.",
                    bookingId, amountToPay, paymentMethod, notificationPref));
            basket.clear();
            loadMoviesAsync(currentSearchKeyword, currentCityFilter,
                    currentGenreFilter, currentYearFilter, currentLanguageFilter);
            new Alert(Alert.AlertType.INFORMATION, "Your Booking ID is: " + bookingId +
                    "\nAll tickets saved.").showAndWait();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Database error: " + e.getMessage()).showAndWait();
        }
    }

    private void goToTeammateLogin(Stage stage) {
        System.out.println("Switching to teammate's login system...");
    }

    public static void main(String[] args) {
        launch(args);
    }
}





