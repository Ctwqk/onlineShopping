package com.taiwei.reggie.Utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Slf4j
@Component
public class ValidateEmailUtils {
    @Autowired
    private JavaMailSender mailSender ;
    private static JavaMailSender staticMailSender;

    @Value(value = "${spring.mail.username}")
    private String from;

    private static String staticFrom;
    @PostConstruct
    public void init() {
        log.info(mailSender.toString());
        staticMailSender = mailSender;
        staticFrom = from;
    }


    public static void sendSimpleEmail(String to, String subject, String text) {
        MimeMessage message = staticMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            helper.setFrom(staticFrom);
            helper.setTo("cui.ta@northeastern.edu");
            helper.setSubject("whatever");
            helper.setText("whatever");
            staticMailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
