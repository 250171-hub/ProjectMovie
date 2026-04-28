package org.example._version;

/**
 * This class represents a single item in the user's shopping basket.
 * It combines the Movie with the specific "Where" and "When".
 */
public class BasketItem {
    private Movie movie;
    private String cinemaName;
    private String hallName;
    private String showTime;
    private String seatNumber;
    private double price;

    public BasketItem(Movie movie, String cinema, String hall, String time, String seat, double price) {
        this.movie = movie;
        this.cinemaName = cinema;
        this.hallName = hall;
        this.showTime = time;
        this.seatNumber = seat;
        this.price = price;
    }

    // Getters
    public Movie getMovie() { return movie; }
    public String getCinemaName() { return cinemaName; }
    public String getShowTime() { return showTime; }
    public String getSeatNumber() { return seatNumber; }
    public double getPrice() { return price; }
    public String getHallName() { return hallName; }
}
