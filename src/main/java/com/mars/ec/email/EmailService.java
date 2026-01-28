package com.mars.ec.email;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender javaMailSender;

    public void sendSimpleEmail(String to) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String fromEmail = dotenv.get("GMAIL_ADDRESS");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Welcome to Shopping Cart, " + to + " !");
        message.setText("Thank you for choosing Shopping Cart. We look forward to serving you.\n\n" +
                "Best regards,\n" +
                "The Shopping Cart Team");
        message.setFrom(fromEmail);

        javaMailSender.send(message);
        System.out.println("Email sent to " + to);
    }
}