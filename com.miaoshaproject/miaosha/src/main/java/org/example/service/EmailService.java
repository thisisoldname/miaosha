package org.example.service;

/***************************
 *Author:ct
 *Time:2020/5/8 17:47
 *Dec:Todo
 ****************************/
public interface EmailService {

    //发送html邮件
    public void senHtmlEmail(String[] receivers, String subject, String content);
}
