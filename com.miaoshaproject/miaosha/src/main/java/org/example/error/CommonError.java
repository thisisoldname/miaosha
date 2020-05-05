package org.example.error;

/***************************
 *Author:ct
 *Time:2020/4/8 11:49
 *Dec:Todo
 ****************************/
public interface CommonError {

    public int getErrCode();
    public String getErrMsg();
    public CommonError setErrMsg(String errMsg);
}
