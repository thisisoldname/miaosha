package org.example.service.model;

import java.math.BigDecimal;

/***************************
 *Author:ct
 *Time:2020/4/12 17:40
 *Dec:Todo
 ****************************/
public class OrderModel {
    //订单号
    private String id;
    //用户id
    private Integer userId;
    //购买的商品id
    private Integer itemId;
    //当时购买商品的单价
    private BigDecimal itemPrice;
    //购买件数
    private Integer amount;
    //购买金额
    private BigDecimal orderPrice;
    //活动信息
    private Integer protoId;

    public Integer getProtoId() {
        return protoId;
    }

    public void setProtoId(Integer protoId) {
        this.protoId = protoId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public BigDecimal getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(BigDecimal orderPrice) {
        this.orderPrice = orderPrice;
    }
}
