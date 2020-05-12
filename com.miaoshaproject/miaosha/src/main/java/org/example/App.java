package org.example;

import org.example.DO.UserDO;
import org.example.dao.UserDOMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = {"org.example"})
@PropertySource(value = "classpath:emailChineseInfo.yaml")
@MapperScan("org.example.dao")
@RestController
public class App {

    @Autowired
    private UserDOMapper userDOMapper;

    @RequestMapping("/")
    public String home(){
        UserDO userDO = userDOMapper.selectByPrimaryKey(1);
        if(userDO == null) {
            return "该用户不存在";
        } else return userDO.getName();
    }

    public static void main(String[] args) {

        SpringApplication.run(App.class, args);
    }
}
