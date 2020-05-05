package org.example.service.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/***************************
 *Author:ct
 *Time:2020/4/11 18:59
 *Dec:Todo
 ****************************/
public class ItemModel implements Serializable {

    private Integer id;
    //商品名
    @NotBlank(message = "商品名称不能为空")
    private String title;
    //价格
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0, message = "商品价格必须大于0")
    private BigDecimal price;
    //库存
    @NotNull(message = "库存不能未填写")
    private Integer stock;
    //描述
    @NotNull(message = "商品描述信息必须不能为空")
    private String description;
    //销量
    private Integer sales;
    //商品描述图片的url
    @NotNull(message = "图片信息不能为空")
    private String imgUrl;
    //秒杀商品信息
    private PromoModel promoModel;

    /*秒杀商品信息*/

    //若非空则以秒杀价格下单
    private Integer promoId;
    //秒杀价格
    private BigDecimal itemPrice;
    //购买数量
    private Integer amount;
    //购买总金额
    private BigDecimal oderPrice;

    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
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

    public BigDecimal getOderPrice() {
        return oderPrice;
    }

    public void setOderPrice(BigDecimal oderPrice) {
        this.oderPrice = oderPrice;
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
