package org.example.service.impl;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.example.DO.ItemDO;
import org.example.DO.ItemStockDO;
import org.example.DO.StockLogDO;
import org.example.dao.ItemDOMapper;
import org.example.dao.ItemStockDOMapper;
import org.example.dao.StockLogDOMapper;
import org.example.error.BussinessException;
import org.example.error.EnumBusinessErr;
import org.example.mq.MqProducer;
import org.example.service.ItemService;
import org.example.service.PromoService;
import org.example.service.model.ItemModel;
import org.example.service.model.PromoModel;
import org.example.validator.ValidationResult;
import org.example.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/***************************
 *Author:ct
 *Time:2020/4/11 19:54
 *Dec:Todo
 ****************************/
@Service
public class ItemServerImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;
    @Autowired
    private ItemDOMapper itemDOMapper;
    @Autowired
    private ItemStockDOMapper itemStockDOMapper;
    @Autowired
    private PromoService promoService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MqProducer mqProducer;
    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BussinessException {

        //校验对象
        ValidationResult validate = validator.validate(itemModel);
        if (validate.isHasErrors()) {

            throw new BussinessException(EnumBusinessErr.PARAMETER_VALIDATION_ERROR, validate.getErrMsg());
        }
        //转化为DO
        ItemDO itemDO = convertItemDOfromItemModel(itemModel);
        //提交数据库
        itemDOMapper.insertSelective(itemDO);
        itemModel.setId(itemDO.getId());
        ItemStockDO itemStockDO = convertItemStockDofromItemModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);
        //返回创建对象
        return convertModelFromDataObject(itemDO, itemStockDO);
    }

    private ItemDO convertItemDOfromItemModel(ItemModel itemModel) {

        if (itemModel == null) return null;

        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel, itemDO);
        itemDO.setPrice(itemModel.getPrice().doubleValue());

        return itemDO;
    }

    private ItemStockDO convertItemStockDofromItemModel(ItemModel itemModel) {

        if (itemModel == null) return null;

        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setStock(itemModel.getStock());
        itemStockDO.setItemId(itemModel.getId());
        return itemStockDO;
    }

    @Override
    public List<ItemModel> listItem() {

        List<ItemDO> itemDOList = itemDOMapper.listItem();
        List<ItemModel> itemModelList = itemDOList.stream().map((o) -> {

            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(o.getId());
            return this.convertModelFromDataObject(o, itemStockDO);
        }).collect(Collectors.toList());

        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {

        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);

        if (itemDO == null) return null;
        //拿到库存数量
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
        if (itemStockDO == null) return null;
        //DO->model
        ItemModel itemModel = convertModelFromDataObject(itemDO, itemStockDO);
        //获得活动信息
        PromoModel promoModel = promoService.getPromoByItemId(id);
        if (promoModel != null && promoModel.getStatus() != 3) {
            itemModel.setPromoModel(promoModel);
        }
        return itemModel;
    }

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) {

//        int res = itemStockDOMapper.decreaseStock(itemId, amount);
        Long res = redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue() * -1);
        if (res < 0) {
            redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue());
            return false;
        } else if(res == 0) {
            //库存售罄
            redisTemplate.opsForValue().set("promo_item_stock_invalid"+itemId, "true");
            return true;
        }

//        boolean result = mqProducer.asyncReduceStock(itemId, amount);
//        if (!result) {
//            redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue());
//            return false;
//        }
        return true;
    }

    @Override
    @Transactional
    public boolean increaseStock(Integer itemId, Integer amount) {

        Long res = redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue());
        return true;
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BussinessException {

        itemDOMapper.increaseSales(itemId, amount);
    }

    @Override
    public ItemModel getItemByIdInCache(Integer id) {

        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_validate_" + id);
        if (itemModel == null) {

            itemModel = getItemById(id);
            redisTemplate.opsForValue().set("item_validate_" + id, itemModel);
            redisTemplate.expire("item_validate_" + id, 10, TimeUnit.MINUTES);
        }
        return itemModel;
    }

    @Override
    public boolean asynDecreaseStock(Integer itemId, Integer amount) {

        boolean result = mqProducer.asyncReduceStock(itemId, amount);
        return result;
    }

    @Override
    @Transactional
    public String initStockLog(Integer itemId, Integer amount) {

        StockLogDO stockLogDO = new StockLogDO();
        stockLogDO.setItemId(itemId);
        stockLogDO.setAmount(amount);
        stockLogDO.setStockLogId(UUID.randomUUID().toString().replace("-", ""));
        stockLogDO.setStatus(1);

        stockLogDOMapper.insert(stockLogDO);

        return stockLogDO.getStockLogId();
    }

    //将do->model
    private ItemModel convertModelFromDataObject(ItemDO itemDO, ItemStockDO itemStockDO) {

        ItemModel itemModel = new ItemModel();

        BeanUtils.copyProperties(itemDO, itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());

        return itemModel;
    }
}
