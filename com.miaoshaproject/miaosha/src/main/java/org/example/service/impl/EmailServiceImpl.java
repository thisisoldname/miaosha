package org.example.service.impl;

import org.example.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/***************************
 *Author:ct
 *Time:2020/5/8 17:47
 *Dec:Todo
 ****************************/
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    JavaMailSender mailSender;

    @Value("${email.send.from}")
    private String from;

    @Override
    public void senHtmlEmail(String[] receivers, String subject, String content) {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            helper.setFrom(from);
            helper.setText(content, false);
            helper.setSubject(subject);
            helper.setTo(receivers);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            //邮件发送失败
            System.out.println(e.fillInStackTrace());
        }
    }
}
