package org.example.controller.VO;

import org.example.service.model.PromoModel;
import org.joda.time.DateTime;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/***************************
 *Author:ct
 *Time:2020/4/11 20:19
 *Dec:Todo
 ****************************/
public class ItemVO implements Serializable {

    private Integer id;
    //商品名
    private String title;
    //价格
    private BigDecimal price;
    //库存
    private Integer stock;
    //描述
    private String description;
    //销量
    private Integer sales;
    //商品描述图片的url
    private String imgUrl;
    //活动状态
    private Integer promoStatus;//0 没有活动
    //活动信息
    private PromoModel promoModel;
    //秒杀活动价
    private BigDecimal promoPrice;
    //秒杀活动ID
    private Integer promoId;
    //活动开始时间
    private String promoStartDate;

    public Integer getPromoStatus() {
        return promoStatus;
    }

    public void setPromoStatus(Integer promoStatus) {
        this.promoStatus = promoStatus;
    }

    public BigDecimal getPromoPrice() {
        return promoPrice;
    }

    public void setPromoPrice(BigDecimal promePrice) {
        this.promoPrice = promePrice;
    }

    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }

    public String getPromoStartDate() {
        return promoStartDate;
    }

    public void setPromoStartDate(String promoStartDate) {
        this.promoStartDate = promoStartDate;
    }

    public PromoModel getPromoModel() {
        return promoModel;
    }

    public void setPromoModel(PromoModel promoModel) {
        this.promoModel = promoModel;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
