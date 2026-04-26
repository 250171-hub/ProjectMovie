package org.example._version;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

// ==========================================
// NOTIFICATION SYSTEM
// ==========================================

public interface NotificationService {
    void sendNotification(String messageContent);
}

class EmailNotification implements NotificationService {

    // Ideally, these should be loaded from a configuration file or environment variables
    private static final String SENDER_EMAIL = "your-email@gmail.com";
    private static final String APP_PASSWORD = "xxxx xxxx xxxx xxxx";
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    private final String recipientEmail;

    /**
     * @param recipientEmail The email address where the notification will be sent.
     */
    public EmailNotification(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    @Override
    public void sendNotification(String messageContent) {
        // 1. Setup SMTP Server Properties
        Properties smtpProperties = new Properties();
        smtpProperties.put("mail.smtp.auth", "true");
        smtpProperties.put("mail.smtp.starttls.enable", "true");
        smtpProperties.put("mail.smtp.host", SMTP_HOST);
        smtpProperties.put("mail.smtp.port", SMTP_PORT);

        // 2. Create Session with Authentication
        Session session = Session.getInstance(smtpProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });

        // 3. Construct and Send the Email in a separate Thread
        // Running on a background thread ensures the JavaFX UI doesn't freeze
        new Thread(() -> {
            try {
                Message emailMessage = new MimeMessage(session);
                emailMessage.setFrom(new InternetAddress(SENDER_EMAIL));
                emailMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                emailMessage.setSubject("Cinema Reserve | Booking Confirmation");
                emailMessage.setText(messageContent);

                Transport.send(emailMessage);
                System.out.println("✅ Email sent successfully to " + recipientEmail);

            } catch (MessagingException e) {
                System.err.println("❌ Failed to send email to " + recipientEmail);
                e.printStackTrace();
            }
        }).start();
    }
}

class SmsNotification implements NotificationService {

    private final String phoneNumber;

    public SmsNotification(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void sendNotification(String messageContent) {
        // Implementation for an SMS Gateway (e.g., Twilio) would go here
        System.out.println("📱 Sending SMS to " + phoneNumber + ": " + messageContent);
    }
}

// ==========================================
// PAYMENT SYSTEM
// ==========================================

interface PaymentMethod {
    boolean processPayment(double amount);
}

class CardPayment implements PaymentMethod {
    private final String cardNumber;

    // BUG FIX: Added the parameter to the constructor
    public CardPayment(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @Override
    public boolean processPayment(double amount) {
        // Mask the card number for security in logs (e.g., **** **** **** 1234)
        String maskedCard = "**** **** **** " + cardNumber.substring(Math.max(0, cardNumber.length() - 4));
        System.out.println("💳 Processing card payment of $" + String.format("%.2f", amount) + " on card " + maskedCard);

        // Assume success for prototype, but real logic would connect to Stripe/PayPal here
        return true;
    }
}

class CashPayment implements PaymentMethod {
    @Override
    public boolean processPayment(double amount) {
        System.out.println("💵 Awaiting cash payment of $" + String.format("%.2f", amount) + " at the counter.");
        return true;
    }
}
