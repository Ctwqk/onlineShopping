package com.taiwei.reggie;

import com.taiwei.reggie.Utils.ValidateEmailUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static com.taiwei.reggie.Utils.ValidateEmailUtils.sendSimpleEmail;

@SpringBootTest
public class EmailSendingTest {
    @Test
    public void testEmailSending(){
        sendSimpleEmail("cui.ta@northeastern.edu", "helo", "helo");
    }
}
