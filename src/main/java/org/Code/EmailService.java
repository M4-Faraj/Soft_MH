package org.Code;

import java.awt.desktop.UserSessionEvent;
import java.util.Iterator;
import java.util.Properties;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailService {
    private final String username;
    private final String password;

    public EmailService(String username , String password){
        this.username = username;
        this.password = password;
    }

    public void sendEmail(String to , String subject , String body){
        Properties props = new Properties();
        props.put("mail.smtp.auth" , "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host" , "smtp.gmail.com");
        props.put("mail.smtp.port" , "587");

        Session session = Session.getInstance(props , new Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(username , password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO , InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email send successfully to + " + to);
        } catch (MessagingException e){
            e.printStackTrace();
            throw new RuntimeException("Failed to email" , e);
        }
    }

    static void run(){
        Dotenv dotenv = Dotenv.load();
        String username = dotenv.get("EMAIL_USERNAME");
        String password = dotenv.get("EMAIL_PASSWORD");

        EmailService emailService = new EmailService(username , password);

        String subject = "Book Due Reminder";
        String body = "Dear user , Your book is due soon. Best regards , An Najah Library System";

        emailService.sendEmail("(email here)" , subject , body);
    }
    public static void  main(String[] args){
            run();
    }


}
