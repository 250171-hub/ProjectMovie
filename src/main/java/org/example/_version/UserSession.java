package org.example._version;

public class UserSession {
    private static UserSession instance;

    private int userId;
    private String username;
    private double coinsBalance;

    private String firstName;
    private String lastName;
    private String phone;
    private String email;

    // Private constructor so it can't be instantiated from outside
    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // Call this method when the user logs in
    public void loginUser(int userId, String username, double coinsBalance, String firstName, String lastName, String phone, String email) {
        this.userId = userId;
        this.username = username;
        this.coinsBalance = coinsBalance;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
    }


    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }



    // Getters and Setters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public double getCoinsBalance() { return coinsBalance; }

    public void setCoinsBalance(double coinsBalance) {
        this.coinsBalance = coinsBalance;
    }

    public void cleanUserSession() {
        this.userId = 0;
        this.firstName = "";
        this.coinsBalance = 0.0;
    }
}