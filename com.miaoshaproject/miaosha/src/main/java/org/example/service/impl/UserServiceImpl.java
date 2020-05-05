package org.example.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.example.DO.UserDO;
import org.example.DO.UserPasswordDO;
import org.example.dao.UserDOMapper;
import org.example.dao.UserPasswordDOMapper;
import org.example.error.BussinessException;
import org.example.error.EnumBusinessErr;
import org.example.service.UserService;
import org.example.service.model.UserModel;
import org.example.validator.ValidationResult;
import org.example.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/***************************
 *Author:ct
 *Time:2020/4/8 10:50
 *Dec:Todo
 ****************************/
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public UserModel getUserById(Integer id) {

        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(id);

        if (userDO == null) return null;

        return convertFromDataObject(userDO, userPasswordDO);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BussinessException {

        if (userModel == null) throw new BussinessException(EnumBusinessErr.PARAMETER_VALIDATION_ERROR);
        ValidationResult validate = validator.validate(userModel);
        if(validate.isHasErrors()) {
            throw new BussinessException(EnumBusinessErr.PARAMETER_VALIDATION_ERROR, validate.getErrMsg());
        }
        //为什么使用insertSelective？当某个属性为null时，不会插入，即采用数据库默认字段
        UserDO userDO = convertUserFromModel(userModel);
        //手机号重复注册
        try {
            userDOMapper.insertSelective(userDO);
        } catch (DuplicateKeyException ex) {
            throw new BussinessException(EnumBusinessErr.PARAMETER_VALIDATION_ERROR, "手机号已被注册");
        }
        userModel.setId(userDO.getId());
        UserPasswordDO userPasswordDO = convertPasswordFromModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);

        return;
    }

    @Override
    public UserModel validateLogin(String telphone, String encrptPassword) throws BussinessException {

        //通过用户手机获取用户信息
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        if (userDO == null) throw new BussinessException(EnumBusinessErr.USER_NOT_EXIST);

        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = convertFromDataObject(userDO, userPasswordDO);
        //比对密码
        if (!StringUtils.equals(userModel.getEncrptPassword(), encrptPassword)) {
            throw new BussinessException(EnumBusinessErr.USER_LOGIN_FAIL);
        }

        return userModel;
    }

    @Override
    public UserModel getUserByIdInCache(Integer id) {

        UserModel userModel = (UserModel) redisTemplate.opsForValue().get("user_validate"+id);
        if(userModel == null) {

            userModel = getUserById(id);
            redisTemplate.opsForValue().set("user_validate_"+id, userModel);
        }
        return userModel;
    }

    private UserDO convertUserFromModel(UserModel userModel) {

        if (userModel == null) return null;

        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel, userDO);

        return userDO;
    }

    private UserPasswordDO convertPasswordFromModel(UserModel userModel) {

        if (userModel == null) return null;

        UserPasswordDO userPasswordDO = new UserPasswordDO();
        BeanUtils.copyProperties(userModel, userPasswordDO);
        userPasswordDO.setUserId(userModel.getId());

        return userPasswordDO;
    }

    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO) {

        if (userDO == null) return null;
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);
        if (userPasswordDO != null) userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());

        return userModel;
    }


}
