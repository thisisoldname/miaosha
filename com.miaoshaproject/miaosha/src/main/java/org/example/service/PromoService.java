package org.example.service;

import org.example.error.BussinessException;
import org.example.service.model.PromoModel;

/***************************
 *Author:ct
 *Time:2020/4/14 17:41
 *Dec:Todo
 ****************************/
public interface PromoService {

    PromoModel getPromoByItemId(Integer itemId);

    //活动发布
    void publishPromo(Integer promoId);

    //生存秒杀令牌
    String generateSecondKillToken(Integer promoId, Integer itemId, Integer userId) throws BussinessException;
}
