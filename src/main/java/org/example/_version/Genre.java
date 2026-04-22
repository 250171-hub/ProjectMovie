package org.example._version;

enum Genre {
    ACTION, COMEDY, DRAMA, SCI_FI, HORROR, ALL
}

class Movie {
    private String title;
    private Genre genre;
    private double price;
    private String imageUrl;
    private String location;
    // Added these missing fields so they can be stored!
    private int year;
    private double rating;
    private String times;

    public Movie(String title, Genre genre, double price, String imageUrl,
                 String location, int year, double rating, String times) {
        this.title = title;
        this.genre = genre;
        this.price = price;
        this.imageUrl = imageUrl;
        this.location = location;
        this.year = year;
        this.rating = rating;
        this.times = times;
    } // Constructor now ends here properly

    // --- Getters ---
    public String getTitle() { return title; }
    public Genre getGenre() { return genre; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getLocation() { return location; }

    // Added missing getters so your UI can display them
    public int getYear() { return year; }
    public double getRating() { return rating; }
    public String getTimes() { return times; }
}
