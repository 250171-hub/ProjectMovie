-- 1. Create the Database
CREATE DATABASE IF NOT EXISTS movie_db;
USE movie_db;

-- 2. Create the Table with the exact columns your Movie class needs
CREATE TABLE IF NOT EXISTS movies (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      title VARCHAR(255) NOT NULL,
                                      genre VARCHAR(50),
                                      price DOUBLE,
                                      image_url VARCHAR(500),
                                      location VARCHAR(100),
                                      release_year INT,
                                      rating DOUBLE,
                                      showtimes VARCHAR(255)
);

-- 3. Add a few starting movies so you can see the grid work immediately
INSERT INTO movies (title, genre, price, image_url, location, release_year, rating, showtimes) VALUES
                                                                                                   ('Deadpool & Wolverine', 'ACTION', 12.50, 'https://m.media-amazon.com/images/M/MV5BZTk5ODY0MmQtMzA3Ni00NGY1LThiYzItZThiNjFiNDM4MTM3XkEyXkFqcGc@._V1_.jpg', 'New York', 2024, 8.1, '14:00, 17:30, 21:00'),
                                                                                                   ('Dune: Part Two', 'SCI_FI', 15.00, 'https://gcp-na-images.contentstack.com/v3/assets/bltea6093859af6183b/blt1ac908201f3c363c/698a48e6cabb1d2dd9e52b75/Dune-Part-Two-Paul.jpeg?branch=production&width=3840&quality=75&auto=webp&crop=3%3A2g', 'London', 2024, 8.6, '13:00, 19:00'),
                                                                                                   ('Oppenheimer', 'DRAMA', 14.00, 'https://m.media-amazon.com/images/M/MV5BN2JkMDc5MGQtZjg3YS00NmFiLWIyZmQtZTJmNTM5MjVmYTQ4XkEyXkFqcGc@._V1_.jpg', 'Tashkent', 2023, 8.4, '15:30, 20:00'),
                                                                                                   ('Inside Out 2', 'COMEDY', 10.00, 'https://upload.wikimedia.org/wikipedia/en/thumb/f/f7/Inside_Out_2_poster.jpg/250px-Inside_Out_2_poster.jpg', 'Tashkent', 2024, 7.8, '10:00, 12:30, 15:00');
UPDATE movies SET release_year = 2024 WHERE id IN (1, 2, 4, 8);
UPDATE movies SET release_year = 2023 WHERE id IN (3, 6, 10);
UPDATE movies SET release_year = 2022 WHERE id IN (7)
