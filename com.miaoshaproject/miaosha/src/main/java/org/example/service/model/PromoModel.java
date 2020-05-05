package org.example.service.model;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.math.BigDecimal;

/***************************
 *Author:ct
 *Time:2020/4/14 17:30
 *Dec:Todo
 ****************************/
public class PromoModel implements Serializable {

    private Integer id;
    //秒杀活动名称
    private String promoName;
    //秒杀活动开始时间
    private DateTime startDate;
    //秒杀活动的结束时间
    private DateTime endDate;
    //秒杀活动使用商品
    private Integer itemId;
    //秒杀活动的商品价格
    private BigDecimal promoItemPrice;
    //秒杀活动状态
    private Integer status;//1，未开始 2，正在进行 3，已结束

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPromoName() {
        return promoName;
    }

    public void setPromoName(String promoName) {
        this.promoName = promoName;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getPromoItemPrice() {
        return promoItemPrice;
    }

    public void setPromoItemPrice(BigDecimal promoItemPrice) {
        this.promoItemPrice = promoItemPrice;
    }
}
