package org.rocman.candidate.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;

@Log4j2
@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String token) {
        try {
            String verifyUrl = "http://localhost:8080/api/auth/verify?token=" + token;
            String subject = "Confirm your account on ROCMAN.org";
            String body = "Hello! Please confirm your new account created on ROCMAN.org by accessing the following link (available 24h):\n\n" + verifyUrl;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent | type=verification | to={} | timestamp={}", toEmail, LocalDateTime.now());
        } catch (Exception e) {
            log.error("Email send failed | type=verification | to={} | timestamp={}",
                    toEmail, LocalDateTime.now(), e); // stack trace
            throw new RuntimeException("Failed to send verification email");
        }
    }

    public void sendResetPasswordEmail(String toEmail, String token) {
        try {
            String link = "http://localhost:8080/api/auth/reset-password?token=" + token;
            String subject = "Password reset on ROCMAN account";
            String body = "You have requested to reset the password on ROCMAN.org account.\n\nPlease access the following link (available 30 minutes):\n" + link;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent | type=password_reset | to={} | timestamp={}", toEmail, LocalDateTime.now());
        } catch (Exception e) {
            log.error("Email send failed | type=password_reset | to={} | timestamp={}",
                    toEmail, LocalDateTime.now(), e); // stack trace
            throw new RuntimeException("Failed to send reset password email");
        }
    }
}
