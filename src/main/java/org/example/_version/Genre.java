package org.example._version;

enum Genre {
    ACTION, COMEDY, DRAMA, SCI_FI, HORROR, ALL
}

class Movie {

    // --- FIELDS ---
    private final String title;
    private final Genre  genre;
    private final double price;
    private final String imageUrl;
    private final String location;
    private final int    releaseYear;
    private final double rating;
    private final String showtimes;
    private final String language;   // ← new field

    // --- CONSTRUCTOR ---
    public Movie(String title, Genre genre, double price, String imageUrl,
                 String location, int releaseYear, double rating,
                 String showtimes, String language) {
        this.title       = title;
        this.genre       = genre;
        this.price       = price;
        this.imageUrl    = imageUrl;
        this.location    = location;
        this.releaseYear = releaseYear;
        this.rating      = rating;
        this.showtimes   = showtimes;
        this.language    = language;
    }

    // --- GETTERS ---
    public String getTitle()       { return title;       }
    public Genre  getGenre()       { return genre;       }
    public double getPrice()       { return price;       }
    public String getImageUrl()    { return imageUrl;    }
    public String getLocation()    { return location;    }
    public int    getReleaseYear() { return releaseYear; }
    public double getRating()      { return rating;      }
    public String getTimes()       { return showtimes;   }
    public String getLanguage()    { return language;    }
}
