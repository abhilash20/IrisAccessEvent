package com.data.projectiris;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailAlertService {


    private final JavaMailSender emailSender;
    private final IrisProperties irisProperties;

    @Autowired
    public EmailAlertService(IrisProperties irisProperties,JavaMailSender emailSender) {
        this.irisProperties = irisProperties;
        this.emailSender = emailSender;
    }



    private static final Logger logger = LoggerFactory.getLogger(EmailAlertService.class);
    public void sendEmailAlert(String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(irisProperties.getAlert_email());  // Set the recipient email address
        mailMessage.setSubject(subject);  // Set the email subject
        mailMessage.setText(message);  // Set the message content

        List<String> cc_emails=irisProperties.getAlert_cc_emails();

        if(cc_emails!=null && !cc_emails.isEmpty()) {
            String[] cc_emailsArr = cc_emails.toArray(new String[0]);
            mailMessage.setCc(cc_emailsArr);
        }
        try {
            emailSender.send(mailMessage);
            logger.info("Email alert sent: {} - {}", subject, message);
        } catch (Exception e) {
            logger.error("Failed to send email alert: {}", e.getMessage(), e);
        }
    }
}
