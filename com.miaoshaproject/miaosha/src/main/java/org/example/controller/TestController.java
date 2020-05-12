package org.example.controller;

import org.example.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/***************************
 *Author:ct
 *Time:2020/5/12 12:47
 *Dec:Todo
 ****************************/
@Controller("/test")
public class TestController {

    @Autowired
    private EmailService emailService;

    @RequestMapping("/email")
    public void email() {

        emailService.senHtmlEmail(new String[]{"2832094127@qq.com"}, "subject", "content");
    }
}
