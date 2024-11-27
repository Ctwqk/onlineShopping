//package com.taiwei.reggie;
//
//import com.taiwei.reggie.Utils.ValidateEmailUtils;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//
//@SpringBootTest
//public class EmailSendingTest {
//
//    @Autowired
//    private JavaMailSender mailSender;
//
//    @Test
//    public void testEmailSending(){
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo("cui.ta@northeastern.edu");
//        message.setSubject("hi cui");
//        message.setText("hi cui");
//        message.setFrom("taiwei293@gmail.com");
//
//        mailSender.send(message);
//    }
//}
