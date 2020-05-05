package org.example.controller;

import com.alibaba.druid.util.StringUtils;
import org.example.controller.VO.UserVO;
import org.example.error.BussinessException;
import org.example.error.EnumBusinessErr;
import org.example.response.CommonReturnType;
import org.example.service.UserService;
import org.example.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/***************************
 *Author:ct
 *Time:2020/4/8 10:48
 *Dec:Todo
 ****************************/
@RestController("user")
@RequestMapping("/user")
//解决跨域不安全
@CrossOrigin(allowCredentials = "true", allowedHeaders = {"*"})
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private RedisTemplate redisTemplate;

    //用户获取otp短信
    @RequestMapping(value = "/getotp", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType getOtp(@RequestParam(name = "telphone") String telphone) {

        //生成otp验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);
        //将otp验证码与用户手机号关联，放入redis
        redisTemplate.opsForValue().set("opt_code_"+telphone, otpCode);
        //有效时间5分钟
        redisTemplate.expire("opt_code_"+telphone, 300, TimeUnit.SECONDS);
        //发送用户，暂时先打印出来
        System.out.println(otpCode);
        return CommonReturnType.create(null);
    }

    @RequestMapping("/get")
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BussinessException {

        UserModel userModel = userService.getUserById(id);
        //如果用户信息不存在
        if (userModel == null) {
            throw new BussinessException(EnumBusinessErr.USER_NOT_EXIST);
        }

        UserVO userVO = convertFromModel(userModel);

        return CommonReturnType.create(userVO);
    }

    //用户登陆接口
    @RequestMapping(value = "/login", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType login(@RequestParam("telphone") String telphone,
                                  @RequestParam("password") String password) throws BussinessException, UnsupportedEncodingException, NoSuchAlgorithmException {

        if(org.apache.commons.lang3.StringUtils.isEmpty(telphone)
                || org.apache.commons.lang3.StringUtils.isEmpty(password)) {
            throw new BussinessException(EnumBusinessErr.PARAMETER_VALIDATION_ERROR);
        }
        //登陆
        UserModel userModel = userService.validateLogin(telphone, this.EncodeByMD5(password));
        //登陆成功，加token
        String uuidToken = UUID.randomUUID().toString().replace("-", "");

        redisTemplate.opsForValue().set(uuidToken, convertFromModel(userModel));
        redisTemplate.expire(uuidToken, 1, TimeUnit.HOURS);

        return CommonReturnType.create(uuidToken);
    }

    private UserVO convertFromModel(UserModel userModel) {

        if (userModel == null) return null;

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);

        return userVO;
    }

    //用户注册接口
    @RequestMapping(value = {"/register"}, method = RequestMethod.POST, consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType register(@RequestParam(name = "telphone") String telphone,
                                     @RequestParam(name = "optCode") String optCode,
                                     @RequestParam(name = "name") String name,
                                     @RequestParam(name = "gender") String gender,
                                     @RequestParam(name = "age") Integer age,
                                     @RequestParam(name = "password") String password) throws BussinessException, UnsupportedEncodingException, NoSuchAlgorithmException {

        //验证验证码是否正确
        String inSessionOptCode = (String) redisTemplate.opsForValue().get("opt_code_"+telphone);
        if (!StringUtils.equals(inSessionOptCode, optCode)) {
            throw new BussinessException(EnumBusinessErr.PARAMETER_VALIDATION_ERROR, "短信验证码错误或者验证码已经过期");
        }

        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setGender(new Byte(gender));
        userModel.setEncrptPassword(EncodeByMD5(password));
        userModel.setRegisterMode("byphone");
        userModel.setTelphone(telphone);

        userService.register(userModel);

        return CommonReturnType.create(null);
    }

    public String EncodeByMD5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();

        String newstr = base64en.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }
}
