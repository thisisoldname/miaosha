package org.example.controller;

import org.example.error.BussinessException;
import org.example.error.EnumBusinessErr;
import org.example.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/***************************
 *Author:ct
 *Time:2020/4/8 13:05
 *Dec:Todo
 ****************************/
@Controller
public class BaseController {

    public static final String CONTENT_TYPE_FORMED = "application/x-www-form-urlencoded";
    //定义exceptionHandler
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public CommonReturnType handlerException(HttpServletRequest request, Exception ex) {

        Map<String, Object> responseData = new HashMap<>();
        if(ex instanceof BussinessException) {

            BussinessException bussinessException = (BussinessException)ex;
            responseData.put("errCode", bussinessException.getErrCode());
            responseData.put("errMsg", bussinessException.getErrMsg());
        } else {

//            System.out.println(ex.getMessage());
            responseData.put("errCode", EnumBusinessErr.UNKNOWN_ERROR.getErrCode());
            responseData.put("errMsg", EnumBusinessErr.UNKNOWN_ERROR.getErrMsg());
        }
        return CommonReturnType.create(responseData, "fail");
    }
}
