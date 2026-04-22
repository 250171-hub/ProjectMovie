package org.example._version;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;


interface NotificationService {
    void sendNotification(String message);
}

class EmailNotification implements NotificationService {
    private final String senderEmail = "your-email@gmail.com";
    private final String appPassword = "xxxx xxxx xxxx xxxx"; // Use an App Password, not your real one
    private String recipientEmail;

    public EmailNotification(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public EmailNotification() {

    }

    @Override
    public void sendNotification(String messageContent) {
        // 1. Setup SMTP Server Properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // 2. Create Session with Authentication
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });

        // 3. Construct and Send the Email in a new Thread
        // (Important: prevents JavaFX UI from freezing)
        new Thread(() -> {
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(senderEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject("Movie Booking Confirmation");
                message.setText(messageContent);

                Transport.send(message);
                System.out.println("✅ Email sent successfully to " + recipientEmail);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }).start();
    }
}

class SmsNotification implements NotificationService {
    @Override
    public void sendNotification(String message) {
        System.out.println("📱 Sending SMS: " + message);
    }
}

// --- Payment System ---
interface PaymentMethod {
    boolean processPayment(double amount);
}

class CardPayment implements PaymentMethod {
    private String cardNumber;
    public CardPayment() { this.cardNumber = cardNumber; }

    @Override
    public boolean processPayment(double amount) {
        System.out.println("💳 Processing card payment of $" + amount + " on card " + cardNumber);
        return true; // Assume success for prototype
    }
}

class CashPayment implements PaymentMethod {
    @Override
    public boolean processPayment(double amount) {
        System.out.println("💵 Awaiting cash payment of $" + amount + " at the counter.");
        return true;
    }
}
