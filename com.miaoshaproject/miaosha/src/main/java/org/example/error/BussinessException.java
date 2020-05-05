package org.example.error;

/***************************
 *Author:ct
 *Time:2020/4/8 11:55
 *Dec:Todo
 ****************************/
public class BussinessException extends Exception implements CommonError{

    public CommonError commonError;

    public BussinessException(CommonError commonError) {
        super();
        this.commonError = commonError;
    }

    public BussinessException(CommonError commonError, String errMsg) {
        super();
        this.commonError = commonError;
        this.commonError.setErrMsg(errMsg);
    }


    @Override
    public int getErrCode() {
        return this.commonError.getErrCode();
    }

    @Override
    public String getErrMsg() {
        return this.commonError.getErrMsg();
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.setErrMsg(errMsg);
        return this;
    }
}
