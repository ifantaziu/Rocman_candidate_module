package org.rocman.candidate.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String token) {
        String verifyUrl = "http://localhost:8080/api/auth/verify?token=" + token;
        String subject = "Confirm your account on ROCMAN.org";
        String body = "Hello! Please confirm your new account created on ROCMAN.org by accessing the following link (available 24h):\n\n" + verifyUrl;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    public void sendResetPasswordEmail(String toEmail, String token) {
        String link = "http://localhost:8080/api/auth/reset-password?token=" + token;
        String subject = "Password reset on ROCMAN account";
        String body = "You have requested to reset the password on ROCMAN.org account.\n\nPlease access the following link (available 30 minutes):\n" + link;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
