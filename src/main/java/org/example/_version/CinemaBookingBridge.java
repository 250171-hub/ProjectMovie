package org.example._version;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class CinemaBookingBridge {

    // ── Theme ─────────────────────────────────────────────────────────────────
    private static final String BG_COLOR              = "#0f0f0f";
    private static final String CARD_BG               = "#1a1a1a";
    private static final String ACCENT_YELLOW         = "#FFCC00";
    private static final String ACCENT_YELLOW_HOVER   = "#e6b800";
    private static final String ACCENT_YELLOW_PRESSED = "#c79f00";

    // ── State ─────────────────────────────────────────────────────────────────
    private final Movie movie;
    private final Stage ownerStage;
    private final Stage dialog;

    private String selectedCity;
    private Show selectedShow;

    private final BorderPane root = new BorderPane();
    private final Button backBtn  = new Button("⬅  BACK");

    // ── Public entry point ────────────────────────────────────────────────────
    public static void open(Movie movie, Stage ownerStage) {
        new CinemaBookingBridge(movie, ownerStage).show();
    }

    private CinemaBookingBridge(Movie movie, Stage ownerStage) {
        this.movie      = movie;
        this.ownerStage = ownerStage;

        dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(ownerStage);
        dialog.setTitle(movie.getTitle());
        dialog.setWidth(1280);
        dialog.setHeight(760);
    }

    private void show() {
        root.setStyle("-fx-background-color: " + BG_COLOR + ";");

        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(18, 40, 18, 40));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #000000; -fx-border-color: #222; -fx-border-width: 0 0 2 0;");

        Label titleLbl = new Label("CINEMA RESERVE");
        titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLbl.setStyle("-fx-text-fill: " + ACCENT_YELLOW + ";");

        applyOutlineStyle(backBtn);
        backBtn.setVisible(false);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().addAll(titleLbl, spacer, backBtn);
        root.setTop(topBar);

        showMovieDetailScreen();

        dialog.setScene(new Scene(root));
        dialog.show();
    }

    // =========================================================================
    // SCREEN 1 — Movie Detail
    // =========================================================================

    private void showMovieDetailScreen() {
        backBtn.setVisible(true);
        backBtn.setOnAction(e -> dialog.close());

        HBox content = new HBox(50);
        content.setPadding(new Insets(50, 80, 50, 80));
        content.setAlignment(Pos.TOP_LEFT);

        // ── Left panel ───────────────────────────────────────────────────────
        VBox leftArea = new VBox(18);
        leftArea.setPrefWidth(320);

        Label largePoster = new Label("MOVIE POSTER");
        largePoster.setPrefSize(300, 400);
        largePoster.setAlignment(Pos.CENTER);
        largePoster.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: #333; -fx-border-color: #333; -fx-border-radius: 15; -fx-background-radius: 15; -fx-font-weight: bold;");

        Label metaTitle = new Label(movie.getTitle().toUpperCase());
        metaTitle.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        String genreStr = movie.getGenre() != null ? movie.getGenre().toString() : "";
        Label metaInfo = new Label(genreStr.toUpperCase() + "  •  " + movie.getReleaseYear());
        metaInfo.setStyle("-fx-text-fill: #888; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label ratingLbl = new Label("⭐ " + movie.getRating() + "  •  " + movie.getLanguage());
        ratingLbl.setStyle("-fx-text-fill: " + ACCENT_YELLOW + "; -fx-font-size: 13px;");

        Label descTitle = new Label("SYNOPSIS");
        descTitle.setStyle("-fx-text-fill: " + ACCENT_YELLOW + "; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label mDesc = new Label("No description available for this movie.");
        mDesc.setWrapText(true);
        mDesc.setMaxWidth(300);
        mDesc.setStyle("-fx-text-fill: #888; -fx-font-size: 14px; -fx-line-spacing: 3;");

        leftArea.getChildren().addAll(largePoster, metaTitle, metaInfo, ratingLbl, descTitle, mDesc);

        // ── Right panel ──────────────────────────────────────────────────────
        VBox rightSide = new VBox(25);
        rightSide.setPrefWidth(700);
        HBox.setHgrow(rightSide, Priority.ALWAYS);

        Label mTitle = new Label(movie.getTitle().toUpperCase());
        mTitle.setFont(Font.font("Arial", FontWeight.BOLD, 38));
        mTitle.setStyle("-fx-text-fill: white;");

        HBox cityRow = new HBox(15);
        cityRow.setAlignment(Pos.CENTER_LEFT);
        Label cityLbl = new Label("CITY:");
        cityLbl.setStyle("-fx-text-fill: " + ACCENT_YELLOW + "; -fx-font-weight: bold; -fx-font-size: 13px;");

        // 👉 YOUR OWN CITIES
        ComboBox<String> cityBox = new ComboBox<>();
        cityBox.getItems().setAll(getCitiesFromDatabase());
        cityBox.setPromptText("Choose City...");
        cityBox.setStyle("-fx-font-size: 14px; -fx-background-color: #222; -fx-text-fill: white;");
        cityBox.setPrefWidth(220);

        cityRow.getChildren().addAll(cityLbl, cityBox);

        Label cinemaSectionTitle = new Label("AVAILABLE CINEMAS");
        cinemaSectionTitle.setStyle("-fx-text-fill: " + ACCENT_YELLOW + "; -fx-font-weight: bold; -fx-font-size: 14px;");

        VBox cinemaListContainer = new VBox(15);
        cinemaListContainer.setFillWidth(true);

        Label pickCityHint = new Label("← Select a city to see available cinemas");
        pickCityHint.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
        cinemaListContainer.getChildren().add(pickCityHint);

        cityBox.setOnAction(e -> {
            selectedCity = cityBox.getValue();
            if (selectedCity != null) loadCinemasIntoView(cinemaListContainer);
        });

        ScrollPane cinemaScroll = new ScrollPane(cinemaListContainer);
        cinemaScroll.setFitToWidth(true);
        cinemaScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        cinemaScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        cinemaScroll.setPrefViewportHeight(460);
        cinemaScroll.setPannable(true);
        cinemaScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(cinemaScroll, Priority.ALWAYS);

        rightSide.getChildren().addAll(mTitle, cityRow, cinemaSectionTitle, cinemaScroll);
        content.getChildren().addAll(leftArea, rightSide);
        root.setCenter(content);
    }

    private void loadCinemasIntoView(VBox container) {
        container.getChildren().clear();

        // 👉 YOUR OWN DATABASE CALL
        List<Cinema> cinemas = getCinemasFromDatabase(selectedCity);

        for (Cinema cinema : cinemas) {
            List<Show> shows = getShowsFromDatabase(cinema.id, movie.getTitle());
            if (shows.isEmpty()) continue;
            container.getChildren().add(buildCinemaCard(cinema, shows));
        }

        if (container.getChildren().isEmpty()) {
            Label emptyLbl = new Label("NO AVAILABLE SHOWTIMES");
            emptyLbl.setStyle("-fx-text-fill: " + ACCENT_YELLOW + "; -fx-font-weight: bold; -fx-font-size: 16px;");
            Label hintLbl = new Label("No cinemas are showing this movie in " + selectedCity + " yet.");
            hintLbl.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
            container.getChildren().add(new VBox(8, emptyLbl, hintLbl));
        }
    }

    private VBox buildCinemaCard(Cinema cinema, List<Show> shows) {
        VBox card = new VBox(16);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: #181818; -fx-padding: 22; -fx-background-radius: 14; -fx-border-color: " + ACCENT_YELLOW + "; -fx-border-radius: 14; -fx-border-width: 1.2;");

        Label cinemaName = new Label(cinema.name.toUpperCase());
        cinemaName.setStyle("-fx-text-fill: " + ACCENT_YELLOW + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        VBox showList = new VBox(12);
        showList.setFillWidth(true);
        loadHallsAndTimes(showList, shows);

        card.getChildren().addAll(cinemaName, showList);
        return card;
    }

    private void loadHallsAndTimes(VBox container, List<Show> shows) {
        container.getChildren().clear();

        Map<String, List<Show>> grouped = shows.stream().collect(Collectors.groupingBy(s -> s.hallName, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<String, List<Show>> entry : grouped.entrySet()) {
            List<Show> hallShows = entry.getValue();
            String type  = hallShows.get(0).hallType.toUpperCase();
            double price = type.contains("VIP") ? 7.50 : 5.00;

            VBox hallCard = new VBox(12);
            applyHallCardHoverStyle(hallCard);

            Label hallLbl = new Label(entry.getKey());
            hallLbl.setStyle("-fx-text-fill: #eee; -fx-font-size: 15px; -fx-font-weight: bold;");

            Label detailLbl = new Label(type + " HALL  •  $" + String.format("%.2f", price));
            detailLbl.setStyle("-fx-text-fill: #888; -fx-font-size: 12px; -fx-font-weight: bold;");

            FlowPane timeRow = new FlowPane(10, 10);
            timeRow.setPrefWrapLength(420);

            for (Show show : hallShows) {
                Button tBtn = new Button(show.time);
                applyAccentStyle(tBtn, "-fx-font-weight: bold; -fx-padding: 10 20;");
                tBtn.setOnAction(e -> {
                    selectedShow = show;
                    showSeatMap(price);
                });
                timeRow.getChildren().add(tBtn);
            }

            hallCard.getChildren().addAll(hallLbl, detailLbl, timeRow);
            container.getChildren().add(hallCard);
        }
    }

    // =========================================================================
    // SCREEN 2 — Seat Map
    // =========================================================================

    private void showSeatMap(double pricePerSeat) {
        backBtn.setOnAction(e -> showMovieDetailScreen());

        HBox seatScreen = new HBox(60);
        seatScreen.setPadding(new Insets(50));
        seatScreen.setAlignment(Pos.CENTER);

        VBox selectionSide = new VBox(20);
        selectionSide.setPrefWidth(220);

        Label summary = new Label(movie.getTitle() + "\n" + selectedShow.time + "\n$" + pricePerSeat + " / seat");
        summary.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        selectionSide.getChildren().add(summary);

        VBox gridArea = new VBox(28);
        gridArea.setAlignment(Pos.CENTER);
        gridArea.setStyle("-fx-background-color: #141414; -fx-padding: 40; -fx-background-radius: 15;");

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setAlignment(Pos.CENTER);

        Set<ShowSeat> selected = new HashSet<>();

        // 👉 YOUR OWN DATABASE CALL FOR SEATS
        ShowSeat[][] seats = getSeatsFromDatabase(selectedShow.id);

        for (int i = 0; i < seats.length; i++) {
            for (int j = 0; j < seats[i].length; j++) {
                ShowSeat s = seats[i][j];
                Button b = new Button(s.row + "" + s.col);
                b.setPrefSize(42, 42);
                updateSeatColor(b, s, false);

                b.setOnAction(e -> {
                    if (s.isBooked) return;
                    if (selected.contains(s)) {
                        selected.remove(s);
                        updateSeatColor(b, s, false);
                    } else {
                        selected.add(s);
                        updateSeatColor(b, s, true);
                    }
                });
                grid.add(b, j, i);
            }
        }

        Button confirmBtn = new Button("CONFIRM BOOKING");
        applyAccentStyle(confirmBtn, "-fx-font-weight: bold; -fx-padding: 15 60;");
        confirmBtn.setOnAction(e -> {
            if (selected.isEmpty()) return;

            // Mark seats as booked in YOUR database!
            bookSeatsInDatabase(selected);

            dialog.close();
        });

        gridArea.getChildren().addAll(grid, confirmBtn);
        seatScreen.getChildren().addAll(selectionSide, gridArea);
        root.setCenter(seatScreen);
    }

    // =========================================================================
    // YOUR OWN DATABASE METHODS (Replace dummy logic with real SQL if needed)
    // =========================================================================

    private List<String> getCitiesFromDatabase() {
        List<String> cities = new ArrayList<>();
        // Example SQL: SELECT DISTINCT city FROM cinemas
        cities.addAll(Arrays.asList("Tashkent", "London", "New York"));
        return cities;
    }

    private List<Cinema> getCinemasFromDatabase(String city) {
        List<Cinema> cinemas = new ArrayList<>();
        // Example SQL: SELECT * FROM cinemas WHERE city = ?
        cinemas.add(new Cinema(1, "Grand Palace " + city));
        return cinemas;
    }

    private List<Show> getShowsFromDatabase(int cinemaId, String movieTitle) {
        List<Show> shows = new ArrayList<>();
        // Example SQL: SELECT * FROM shows WHERE cinema_id = ? AND movie_title = ?
        shows.add(new Show(1, "Hall A", "Standard", "18:00"));
        shows.add(new Show(2, "Hall B", "VIP", "20:30"));
        return shows;
    }

    private ShowSeat[][] getSeatsFromDatabase(int showId) {
        // Example SQL: SELECT * FROM seats WHERE show_id = ?
        // For now, generating a fake 5x5 grid
        ShowSeat[][] grid = new ShowSeat[5][5];
        char rowLabel = 'A';
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                grid[i][j] = new ShowSeat(showId, String.valueOf(rowLabel), j + 1, false);
            }
            rowLabel++;
        }
        return grid;
    }

    private void bookSeatsInDatabase(Set<ShowSeat> selectedSeats) {
        // Example SQL: UPDATE seats SET is_booked = true WHERE show_id = ? AND row_letter = ? AND col_num = ?
        System.out.println("Booking " + selectedSeats.size() + " seats in YOUR database!");
    }


    // =========================================================================
    // UI HELPERS
    // =========================================================================

    private void updateSeatColor(Button b, ShowSeat s, boolean isSelected) {
        if (s.isBooked) {
            b.setStyle("-fx-background-color: #333; -fx-text-fill: #555;");
            b.setDisable(true);
        } else if (isSelected) {
            applyAccentStyle(b, "-fx-padding: 0; -fx-background-radius: 6; -fx-font-weight: bold;");
        } else {
            applySeatOutlineStyle(b);
        }
    }

    private void applyAccentStyle(Button btn, String extra) {
        btn.setStyle("-fx-background-color: " + ACCENT_YELLOW + "; -fx-text-fill: black; -fx-cursor: hand; -fx-background-radius: 8;" + extra);
    }
    private void applyOutlineStyle(Button btn) {
        btn.setStyle("-fx-font-size: 14px; -fx-cursor: hand; -fx-background-color: transparent; -fx-text-fill: " + ACCENT_YELLOW + "; -fx-border-color: " + ACCENT_YELLOW + "; -fx-border-radius: 5; -fx-padding: 8 15; -fx-font-weight: bold;");
    }
    private void applySeatOutlineStyle(Button btn) {
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #444; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
    }
    private void applyHallCardHoverStyle(VBox card) {
        card.setStyle("-fx-background-color: #222222; -fx-padding: 16 18; -fx-background-radius: 10; -fx-border-color: #303030; -fx-border-radius: 10;");
    }

    // =========================================================================
    // YOUR OWN INDEPENDENT DATA CLASSES
    // (We put them here so you don't have to create 3 new Java files)
    // =========================================================================

    public static class Cinema {
        public int id;
        public String name;
        public Cinema(int id, String name) { this.id = id; this.name = name; }
    }

    public static class Show {
        public int id;
        public String hallName;
        public String hallType;
        public String time;
        public Show(int id, String hallName, String hallType, String time) {
            this.id = id; this.hallName = hallName; this.hallType = hallType; this.time = time;
        }
    }

    public static class ShowSeat {
        public int showId;
        public String row;
        public int col;
        public boolean isBooked;
        public ShowSeat(int showId, String row, int col, boolean isBooked) {
            this.showId = showId; this.row = row; this.col = col; this.isBooked = isBooked;
        }
    }
}
