package org.example.service.impl;

import org.apache.logging.log4j.message.ReusableMessage;
import org.example.DO.PromoDO;
import org.example.dao.PromoDOMapper;
import org.example.error.BussinessException;
import org.example.error.EnumBusinessErr;
import org.example.service.ItemService;
import org.example.service.PromoService;
import org.example.service.UserService;
import org.example.service.model.ItemModel;
import org.example.service.model.PromoModel;
import org.example.service.model.UserModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/***************************
 *Author:ct
 *Time:2020/4/14 17:44
 *Dec:Todo
 ****************************/
@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;
    @Autowired
    private ItemService itemService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;


    @Override
    public PromoModel getPromoByItemId(Integer itemId) {

        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);
        PromoModel promoModel = convertFromDataObject(promoDO);
        //判断秒杀活动是否即将开始或者正在开始
        if (promoModel == null) return null;
        DateTime now = DateTime.now();
        if (promoModel.getStartDate().isAfterNow()) {
            //未开始
            promoModel.setStatus(1);
        } else if (promoModel.getStartDate().isBeforeNow() && promoModel.getEndDate().isAfterNow()) {
            //正在进行
            promoModel.setStatus(2);
        } else {
            //已结束
            promoModel.setStatus(3);
        }

        return promoModel;
    }

    @Override
    public void publishPromo(Integer promoId) {

        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        if (promoDO == null || promoDO.getItemId() == null || promoDO.getItemId().intValue() == 0) {
            return;
        }
        ItemModel itemModel = itemService.getItemById(promoDO.getItemId());
        //redis缓存商品库存数量
        redisTemplate.opsForValue().set("promo_item_stock_" + itemModel.getId(), itemModel.getStock());
        //设置大闸限制数量
        redisTemplate.opsForValue().set("promo_door_count_" + itemModel.getId(), itemModel.getStock() * 5);
    }

    @Override
    public String generateSecondKillToken(Integer promoId, Integer itemId, Integer userId) throws BussinessException {

        //判断库存是否售罄
        Object invalid = redisTemplate.opsForValue().get("promo_item_stock_invalid" + itemId);
        if (invalid != null) {
            return null;
        }

        //商品是否存在
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if (itemModel == null) {
            return null;
        }
        //用户是否存在
        UserModel userModel = userService.getUserByIdInCache(userId);
        if (userModel == null) {
            return null;
        }
        //获取大闸数量，判断是否给令牌
        long count = redisTemplate.opsForValue().increment("promo_door_count_" + itemId, -1);
        if (count < 0) {
            redisTemplate.opsForValue().increment("promo_door_count_" + itemId, 1);
            return null;
        }
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        PromoModel promoModel = convertFromDataObject(promoDO);
        //判断秒杀活动是否即将开始或者正在开始
        if (promoModel == null) return null;
        DateTime now = DateTime.now();
        if (promoModel.getStartDate().isAfterNow()) {
            //未开始
            promoModel.setStatus(1);
        } else if (promoModel.getStartDate().isBeforeNow() && promoModel.getEndDate().isAfterNow()) {
            //正在进行
            promoModel.setStatus(2);
        } else {
            //已结束
            promoModel.setStatus(3);
        }
        if (promoModel.getStatus() != 2) return null;
        //生成令牌
        String token = UUID.randomUUID().toString().replace("-", "");

        redisTemplate.opsForValue().set("promo_token_" + promoId + "_" + itemId + "_" + userId, token);
        redisTemplate.expire("promo_token_" + promoId + "_" + itemId + "_" + userId, 5, TimeUnit.MINUTES);

        return token;
    }

    private PromoModel convertFromDataObject(PromoDO promoDO) {

        if (promoDO == null) return null;

        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));

        return promoModel;
    }
}
