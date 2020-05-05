package org.example.service;

import org.example.error.BussinessException;
import org.example.service.model.OrderModel;

/***************************
 *Author:ct
 *Time:2020/4/12 18:26
 *Dec:Todo
 ****************************/
public interface OrderService {
    //1 通过前端传来秒杀活动Id，下单接口校验id是否对应商品，是否商品已经开始
    //2 直接在下单的接口中判断对应的商品是否在秒杀活动中，若在，则以秒杀价格下单

    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) throws BussinessException;
}
