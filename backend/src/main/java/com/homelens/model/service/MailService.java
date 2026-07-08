package com.homelens.model.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MailService {
    private final ExecutorService mailExecutor = Executors.newFixedThreadPool(5);

    private final String senderEmail;
    private final String senderPassword;
    private final String verifyBaseUrl;

    public MailService(
            @Value("${mail.sender.email:${MAIL_SENDER_EMAIL:}}") String senderEmail,
            @Value("${mail.sender.password:${MAIL_APP_PASSWORD:}}") String senderPassword,
            @Value("${app.verify-base-url:${APP_VERIFY_BASE_URL:http://localhost}}") String verifyBaseUrl) {
        this.senderEmail = senderEmail;
        this.senderPassword = senderPassword;
        this.verifyBaseUrl = verifyBaseUrl;
    }

    public void sendResetPassword(String email, String newPassword) {
        String subject = "HomeLens temporary password";
        String body = "HomeLens temporary password\n\n"
                + "Temporary password: " + newPassword + "\n\n"
                + "Please change your password after login.";

        sendTextAsync(email, subject, body);
    }

    public void sendVerifyEmailAsync(String email, String verificationToken) {
        String verifyUrl = verifyBaseUrl
                + "/api/v1/auth/email/verify?token="
                + URLEncoder.encode(verificationToken, StandardCharsets.UTF_8);

        String html = ""
                + "<div style='font-family:Arial,sans-serif; line-height:1.6;'>"
                + "  <h2>HomeLens email verification</h2>"
                + "  <p>Please click the button below to verify your email.</p>"
                + "  <p style='margin:24px 0;'>"
                + "    <a href='" + verifyUrl + "' "
                + "       style='display:inline-block; padding:12px 18px; background:#2563eb; color:#fff; "
                + "              text-decoration:none; border-radius:8px;'>"
                + "      Verify email"
                + "    </a>"
                + "  </p>"
                + "  <p style='font-size:12px; color:#666;'>If the button does not work, copy this link into your browser.</p>"
                + "  <p style='font-size:12px; color:#666; word-break:break-all;'>" + verifyUrl + "</p>"
                + "</div>";

        sendHtmlAsync(email, "HomeLens email verification", html);
    }

    private void sendTextAsync(String recipientEmail, String subject, String body) {
        mailExecutor.submit(() -> {
            try {
                Message message = createMessage(recipientEmail, subject);
                message.setText(body);
                Transport.send(message);
                log.info("Email sent to {}", recipientEmail);
            } catch (Exception e) {
                log.error("Failed to send email to {}", recipientEmail, e);
            }
        });
    }

    private void sendHtmlAsync(String recipientEmail, String subject, String html) {
        mailExecutor.submit(() -> {
            try {
                Message message = createMessage(recipientEmail, subject);
                message.setContent(html, "text/html; charset=UTF-8");
                Transport.send(message);
                log.info("Email sent to {}", recipientEmail);
            } catch (Exception e) {
                log.error("Failed to send email to {}", recipientEmail, e);
            }
        });
    }

    private Message createMessage(String recipientEmail, String subject) throws MessagingException {
        validateMailSettings();

        Message message = new MimeMessage(createSession());
        try {
            message.setFrom(new InternetAddress(senderEmail, "HomeLens"));
        } catch (UnsupportedEncodingException e) {
            message.setFrom(new InternetAddress(senderEmail));
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject(subject);
        return message;
    }

    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });
    }

    private void validateMailSettings() {
        if (senderEmail == null || senderEmail.isBlank()) {
            throw new IllegalStateException("MAIL_SENDER_EMAIL is not configured.");
        }
        if (senderPassword == null || senderPassword.isBlank()) {
            throw new IllegalStateException("MAIL_APP_PASSWORD is not configured.");
        }
    }

    @PreDestroy
    public void shutdown() {
        mailExecutor.shutdown();
    }
}