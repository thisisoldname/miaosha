package org.example.service;

import org.example.error.BussinessException;
import org.example.service.model.UserModel;

/***************************
 *Author:ct
 *Time:2020/4/8 10:50
 *Dec:Todo
 ****************************/
public interface UserService {

    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BussinessException;
    UserModel validateLogin(String telphone, String password) throws BussinessException;
    //通过缓存获取用户对象
    UserModel getUserByIdInCache(Integer id);
}
