package com.data.projectiris;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Service
public class SendEmail {

    @Autowired
    private JavaMailSender emailSender;
    private String alertRecipientEmail="yabhilash1994@gmail.com";
    private static final Logger logger = LoggerFactory.getLogger(ChangeListenerService.class);
    public void sendEmailAlert(String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(alertRecipientEmail);  // Set the recipient email address
        mailMessage.setSubject(subject);  // Set the email subject
        mailMessage.setText(message);  // Set the message content
        try {
            emailSender.send(mailMessage);
            logger.info("Email alert sent: {} - {}", subject, message);
        } catch (Exception e) {
            logger.error("Failed to send email alert: {}", e.getMessage(), e);
        }
    }
}
