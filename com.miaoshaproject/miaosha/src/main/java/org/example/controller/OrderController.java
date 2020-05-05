package org.example.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.sun.tools.javac.jvm.Items;
import org.apache.commons.lang3.StringUtils;
import org.example.controller.VO.UserVO;
import org.example.error.BussinessException;
import org.example.error.EnumBusinessErr;
import org.example.mq.MqProducer;
import org.example.response.CommonReturnType;
import org.example.service.ItemService;
import org.example.service.OrderService;
import org.example.service.PromoService;
import org.example.service.model.OrderModel;
import org.example.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.*;

/***************************
 *Author:ct
 *Time:2020/4/12 20:47
 *Dec:Todo
 ****************************/
@RestController("order")
@RequestMapping("/order")
@CrossOrigin(allowCredentials = "true", origins = {"*"})
public class OrderController extends BaseController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MqProducer mqProducer;
    @Autowired
    private ItemService itemService;
    @Autowired
    private PromoService promoService;

    private RateLimiter orderCreateRateLimiter;

    @Value("${order.limit-num}")
    private int limitNum;
    @PostConstruct
    public void init() {

        orderCreateRateLimiter = RateLimiter.create(limitNum);
    }

    @RequestMapping(value = "/createOrder", method = RequestMethod.POST, consumes = CONTENT_TYPE_FORMED)
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "amount") Integer amount,
                                        @RequestParam(name = "promoId", required = false) Integer promoId,
                                        @RequestParam(name = "promoToken", required = false) String promoToken) throws BussinessException {

        if (!orderCreateRateLimiter.tryAcquire()) {
            throw new BussinessException(EnumBusinessErr.RATELIMIT);
        }
        String token = request.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token))
            throw new BussinessException(EnumBusinessErr.USER_NOT_LOGIN);
        UserVO loginUser = (UserVO) redisTemplate.opsForValue().get(token);
        if (loginUser == null)
            throw new BussinessException(EnumBusinessErr.USER_NOT_LOGIN);
        //校验秒杀令牌
        if (promoToken != null) {
            String promoTokenInRedis = (String) redisTemplate.opsForValue().get("promo_token_" + promoId + "_" + itemId + "_" + loginUser.getId());
            if (!StringUtils.equals(promoToken, promoTokenInRedis)) {
                throw new BussinessException(EnumBusinessErr.PARAMETER_VALIDATION_ERROR, "秒杀令牌校验失败");
            }
        }
        //加入库存流水
        String stockLogId = itemService.initStockLog(itemId, amount);
        //下单
        if (!mqProducer.transationAsynReduceStock(loginUser.getId(), promoId, itemId, amount, stockLogId)) {
            throw new BussinessException(EnumBusinessErr.STOCK_NOT_ENOUGH);
        }
//            }
//        });

//        try {
//            Object o = future.get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            throw new BussinessException(EnumBusinessErr.UNKNOWN_ERROR);
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//            throw new BussinessException(EnumBusinessErr.UNKNOWN_ERROR);
//        }
        return CommonReturnType.create(null);
    }

    //生成秒杀令牌
    @RequestMapping(value = "/generateToken", method = RequestMethod.POST, consumes = CONTENT_TYPE_FORMED)
    public CommonReturnType generateToken(@RequestParam(name = "itemId") Integer itemId,
                                          @RequestParam(name = "promoId", required = false) Integer promoId) throws BussinessException {

        //根据token获取用户信息
        String token = request.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token))
            throw new BussinessException(EnumBusinessErr.USER_NOT_LOGIN);
        UserVO loginUser = (UserVO) redisTemplate.opsForValue().get(token);
        if (loginUser == null)
            throw new BussinessException(EnumBusinessErr.USER_NOT_LOGIN);

        String promoToken = promoService.generateSecondKillToken(promoId, itemId, loginUser.getId());
        if (promoToken == null) {
            throw new BussinessException(EnumBusinessErr.PARAMETER_VALIDATION_ERROR, "生成令牌失败");
        }
        return CommonReturnType.create(promoToken);
    }
}
