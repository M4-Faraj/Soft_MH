package org.Code;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
/**
 * A utility class responsible for sending emails through Gmail's SMTP server.
 * <p>
 * This class supports:
 * <ul>
 *     <li>Sending plain-text email messages</li>
 *     <li>Loading SMTP credentials from a <code>.env</code> file</li>
 *     <li>Configuring secure SMTP properties (TLS)</li>
 * </ul>
 * It is used in the library system to deliver notifications such as book reminders,
 * loan confirmations, or overdue alerts.
 *
 * @author HamzaAbdulsalam & Mohammad Dhillieh
 * @version 1.0
 */
public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int    SMTP_PORT = 587;

    private final String username;
    private final String password;

    // ----------------- ctor -----------------
    public EmailService(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("SMTP credentials must not be null");
        }
        this.username = username;
        this.password = password;
    }

    // ----------------- factory from .env -----------------
    public static EmailService fromEnv() {
        Dotenv dotenv = Dotenv.load();
        String user = dotenv.get("EMAIL_USERNAME");
        String pass = dotenv.get("EMAIL_PASSWORD");

        if (user == null || pass == null) {
            throw new IllegalStateException(
                    "EMAIL_USERNAME or EMAIL_PASSWORD is missing in .env"
            );
        }
        return new EmailService(user, pass);
    }

    // ----------------- send email -----------------
    public void sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Recipient email must not be empty");
        }

        Session session = Session.getInstance(buildSmtpProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email sent successfully to " + to);
        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + to);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    // ----------------- helpers -----------------
    private Properties buildSmtpProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        return props;
    }

    // ----------------- quick manual test -----------------
    public static void main(String[] args) {
        EmailService emailService = EmailService.fromEnv();

        String subject = "Book Due Reminder";
        String body = "Dear user, your book is due soon.\nBest regards,\nAn Najah Library System";

        // غيّر الإيميل هون بالتست تبعك
        emailService.sendEmail("someone@example.com", subject, body);
    }
}
