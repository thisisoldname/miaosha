package org.example.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/***************************
 *Author:ct
 *Time:2020/4/11 18:16
 *Dec:Todo
 ****************************/
@Component
public class ValidatorImpl implements InitializingBean {

    private Validator validator;

    //实现校验方法并返回方法结果
    public ValidationResult validate(Object bean) {

        ValidationResult result = new ValidationResult();
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);
        if(constraintViolationSet.size()>0) {

            //有错误
            result.setHasErrors(true);
            for(ConstraintViolation constraintViolation:constraintViolationSet) {

                String errMsg = constraintViolation.getMessage();
                String propertyName = constraintViolation.getPropertyPath().toString();
                result.getErrorMsgMap().put(propertyName, errMsg);
            }
        }
        return result;
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        //将hibernate validator通过工厂的初始化方式，使其实例化
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
