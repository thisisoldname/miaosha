package org.example.service;

import org.example.error.BussinessException;
import org.example.service.model.ItemModel;

import java.util.List;

/***************************
 *Author:ct
 *Time:2020/4/11 19:51
 *Dec:Todo
 ****************************/
public interface ItemService {

    //创建商品
    ItemModel createItem(ItemModel itemModel) throws BussinessException;

    //商品列表浏览
    List<ItemModel> listItem();

    //商品详情浏览
    ItemModel getItemById(Integer id);

    //扣减库存
    boolean decreaseStock(Integer itemId, Integer amount);

    //扣减库存
    boolean increaseStock(Integer itemId, Integer amount);

    //增加销量
    void increaseSales(Integer itemId, Integer amount) throws BussinessException;

    //item promo 缓存
    ItemModel getItemByIdInCache(Integer id);

    //异步更新库存
    boolean asynDecreaseStock(Integer itemId, Integer amount);

    //初始化库存流水
    String initStockLog(Integer itemId, Integer amount);
}
