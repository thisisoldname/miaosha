package org.example.response;

/***************************
 *Author:ct
 *Time:2020/4/8 11:26
 *Dec:Todo
 ****************************/
public class CommonReturnType {

    private String status;
    private Object data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static CommonReturnType create(Object result) {

        return CommonReturnType.create(result, "success");
    }

    public static CommonReturnType create(Object result, String s) {

        CommonReturnType type = new CommonReturnType();
        type.setStatus(s);
        type.setData(result);

        return type;
    }


}
