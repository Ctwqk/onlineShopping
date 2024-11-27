package com.taiwei.reggie.Utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

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
        staticFrom = staticFrom;
    }


    public static void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom(staticFrom);
        staticMailSender.send(message);
    }
}
