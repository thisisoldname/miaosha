package org.example.service.impl;

import com.sun.tools.corba.se.idl.constExpr.Or;
import org.example.DO.ItemStockDO;
import org.example.DO.OrderDO;
import org.example.DO.SequenceDO;
import org.example.DO.StockLogDO;
import org.example.dao.ItemStockDOMapper;
import org.example.dao.OrderDOMapper;
import org.example.dao.SequenceDOMapper;
import org.example.dao.StockLogDOMapper;
import org.example.error.BussinessException;
import org.example.error.EnumBusinessErr;
import org.example.mq.MqProducer;
import org.example.service.ItemService;
import org.example.service.OrderService;
import org.example.service.UserService;
import org.example.service.model.ItemModel;
import org.example.service.model.OrderModel;
import org.example.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/***************************
 *Author:ct
 *Time:2020/4/12 18:28
 *Dec:Todo
 ****************************/
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderDOMapper orderDOMapper;
    @Autowired
    private SequenceDOMapper sequenceDOMapper;
    @Autowired
    private StockLogDOMapper stockLogDOMapper;
    @Autowired
    private MqProducer mqProducer;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, String userName, Integer itemId, Integer promoId, Integer amount, String stockLogId, String userEmail) throws BussinessException {

        //校验下单状态，商品是否存在，用户是否合法，购买数量是否合法
//        ItemModel itemModel = itemService.getItemById(itemId);
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);

        if (amount <= 0 || amount > 99) {
            throw new BussinessException(EnumBusinessErr.PARAMETER_VALIDATION_ERROR, "数量信息不正确");
        }

        //落单减库存、支付减库存
        boolean res = itemService.decreaseStock(itemId, amount);
        if (!res) throw new BussinessException(EnumBusinessErr.STOCK_NOT_ENOUGH);
        //订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setAmount(amount);
        orderModel.setItemId(itemId);
        orderModel.setProtoId(promoId);
        if (promoId != null) {
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        } else {
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
        //生成交易流水号
        orderModel.setId(this.generateOrderNo());
        OrderDO orderDO = convertFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);
        //增加销量
        itemService.increaseSales(itemId, amount);
        //更新流水状态
        StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
        if (stockLogDO == null) {
            throw new BussinessException(EnumBusinessErr.UNKNOWN_ERROR);
        }
        stockLogDO.setStatus(2);
        stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);

        //异步发送 购买成功 邮件
        if (userEmail != null)
            mqProducer.asyncSendBuySuccessEmail(orderModel.getId(), userName, itemModel.getTitle(), userEmail);
//        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//            @Override
//            public void afterCommit() {
//                super.afterCommit();
//                //异步mq更新库存
//                boolean mqResult = itemService.asynDecreaseStock(itemId, amount);
////                if(!mqResult) {
////                    itemService.increaseSales(itemId, amount);
////                    throw new BussinessException(EnumBusinessErr.MQ_SEND_FAIL);
////                }
//            }
//        });

        //返回前端
        return orderModel;
    }

    private OrderDO convertFromOrderModel(OrderModel orderModel) {

        if (orderModel == null) return null;
        OrderDO orderDO = new OrderDO();

        BeanUtils.copyProperties(orderModel, orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());

        return orderDO;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderNo() {

        //订单号16位
        StringBuilder sb = new StringBuilder();
        //前8位时间信息，年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        sb.append(nowDate);
        //中间位为自增序列
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        Integer sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequence + sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKey(sequenceDO);
        String sequenceStr = String.valueOf(sequence);
        //凑足六位
        for (int i = 0; i < 6 - sequenceStr.length(); i++) sb.append("0");
        sb.append(sequenceStr);
        //最后两位为分库分表位，暂时写死
        sb.append("00");

        return sb.toString();
    }
}
