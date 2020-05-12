package org.example.controller;

import org.apache.commons.lang3.math.IEEE754rUtils;
import org.example.controller.VO.ItemVO;
import org.example.error.BussinessException;
import org.example.response.CommonReturnType;
import org.example.service.CacheService;
import org.example.service.ItemService;
import org.example.service.PromoService;
import org.example.service.model.ItemModel;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/***************************
 *Author:ct
 *Time:2020/4/11 20:18
 *Dec:Todo
 ****************************/
@RestController("item")
@RequestMapping("/item")
@CrossOrigin(allowCredentials = "true", origins = {"*"})
public class ItemController extends BaseController {

    @Autowired
    private ItemService itemService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private PromoService promoService;

    //创建商品Controller
    @RequestMapping(value = "/createItem", method = RequestMethod.POST, consumes = CONTENT_TYPE_FORMED)
    public CommonReturnType createItem(@RequestParam(name = "title") String title,
                                       @RequestParam(name = "description") String description,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock") Integer stock,
                                       @RequestParam(name = "imgUrl") String imgUrl) throws BussinessException {
        //封装
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);
        ItemModel item = itemService.createItem(itemModel);

        ItemVO itemVO = convertVOFromModel(itemModel);

        return CommonReturnType.create(itemVO);
    }

    //商品列表页面浏览
    @RequestMapping(value = "/listItem", method = RequestMethod.GET)
    public CommonReturnType listItem() {

        List<ItemModel> itemModels = itemService.listItem();

        List<ItemVO> itemVOList = itemModels.stream().map(o -> {

            return convertVOFromModel(o);
        }).collect(Collectors.toList());

        return CommonReturnType.create(itemVOList);
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public CommonReturnType getItem(@RequestParam(value = "id") Integer id) {

        ItemModel itemModel;
        //本地缓存中找
        itemModel = (ItemModel) cacheService.getFromCommonCache("item_" + id);
        //若本地不存在
        if (itemModel == null) {

            //redis中寻找
            itemModel = (ItemModel) redisTemplate.opsForValue().get("item_" + id);
            //若redis不存在
            if (itemModel == null) {

                //数据库中找
                itemModel = itemService.getItemById(id);
                //填充到reids
                redisTemplate.opsForValue().set("item_" + id, itemModel);
                redisTemplate.expire("item_" + id, 10, TimeUnit.MINUTES);
            }
            //填充到本地缓存
            cacheService.setCommonCache("item_" + id, itemModel);
        }
        ItemVO itemVO = convertVOFromModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

    //发布秒杀活动
    @RequestMapping(value = "/publishPromo", method = RequestMethod.GET)
    public CommonReturnType publishPromo(@RequestParam("promoId") Integer promoId) {

        promoService.publishPromo(promoId);

        return CommonReturnType.create(null);
    }

    private ItemVO convertVOFromModel(ItemModel itemModel) {

        if (itemModel == null) return null;
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);

        if (itemModel.getPromoModel() != null) {
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
            itemVO.setPromoStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
        } else itemVO.setPromoStatus(0);
        return itemVO;
    }

}
