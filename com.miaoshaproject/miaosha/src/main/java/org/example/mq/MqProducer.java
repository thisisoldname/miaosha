package org.example.mq;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.example.DO.StockLogDO;
import org.example.dao.StockLogDOMapper;
import org.example.error.BussinessException;
import org.example.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/***************************
 *Author:ct
 *Time:2020/4/21 22:17
 *Dec:Todo
 ****************************/
@Component
public class MqProducer {

    @Autowired
    private OrderService orderService;
    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    private DefaultMQProducer producer;

    private TransactionMQProducer transactionMQProducer;

    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topic.stock}")
    private String stockTopic;

    @Value("${mq.topic.email}")
    private String emailTopic;

//    @Value("${email.item.buy.success.subject}")
    private String buySuccessSubject = "商品购买成功";

//    @Value("${email.item.buy.success.content}")
    private String buySuccessContent = "用户%s：\\n您好，您购买的商品<span style=\"color:red\">%s<strong>，已下单成功，订单号%s，请尽快支付\n";

    @PostConstruct
    public void init() throws MQClientException {

        producer = new DefaultMQProducer("producer_group");
        producer.setNamesrvAddr(nameAddr);
        producer.setSendMsgTimeout(6000);
        producer.start();

        transactionMQProducer = new TransactionMQProducer("transation_producer_group");
        transactionMQProducer.setNamesrvAddr(nameAddr);
        transactionMQProducer.setSendMsgTimeout(6000);
        transactionMQProducer.start();

        transactionMQProducer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object args) {

                Integer userId = (Integer) ((Map) args).get("userId");
                Integer promoId = (Integer) ((Map) args).get("promoId");
                Integer itemId = (Integer) ((Map) args).get("itemId");
                Integer amount = (Integer) ((Map) args).get("amount");
                String stockLogId = (String) ((Map) args).get("stockLogId");
                String userEmail = (String) ((Map) args).get("userEmail");
                String userName = (String) ((Map) args).get("userName");
                try {
                    orderService.createOrder(userId, userName, itemId, promoId, amount, stockLogId, userEmail);
                } catch (BussinessException e) {

                    //设置stockLog为回滚状态
                    StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                    stockLogDO.setStatus(3);
                    stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {

                Message message = messageExt;
                String jsonString = new String(message.getBody());
                Map<String, Object> map = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) map.get("itemId");
                Integer amount = (Integer) map.get("amount");
                String stockLogId = (String) map.get("stockLogId");
                StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                if (stockLogDO == null) {
                    return LocalTransactionState.UNKNOW;
                }

                if (stockLogDO.getStatus().intValue() == 2) return LocalTransactionState.COMMIT_MESSAGE;
                else if (stockLogDO.getStatus().intValue() == 1) return LocalTransactionState.UNKNOW;
                else return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        });
    }

    //同步库存扣减消息
    public boolean asyncReduceStock(Integer itemId, Integer amount) {

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        Message message = new Message(stockTopic, "increase", JSON.toJSON(bodyMap).toString().getBytes(Charset.forName("utf-8")));

        try {
            producer.send(message);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        } catch (RemotingException e) {
            e.printStackTrace();
            return false;
        } catch (MQBrokerException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //异步 同步事务扣减消息
    public boolean transationAsynReduceStock(Integer userId, Integer promoId, Integer itemId, Integer amount, String stockLogId, String userName, String itemTitle, String email) {

        //body 提交到mq
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        bodyMap.put("stockLogId", stockLogId);

        //args 用于事务的处理
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("itemId", itemId);
        argsMap.put("amount", amount);
        argsMap.put("userId", userId);
        argsMap.put("promoId", promoId);
        argsMap.put("userEmail", email);
        argsMap.put("userName", userName);
        argsMap.put("itemTitle", itemTitle);
        argsMap.put("stockLogId", stockLogId);
        Message message = new Message(stockTopic, "increase", JSON.toJSON(bodyMap).toString().getBytes(Charset.forName("utf-8")));

        TransactionSendResult sendResult = null;
        try {
            sendResult = transactionMQProducer.sendMessageInTransaction(message, argsMap);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        }
        if (sendResult.getLocalTransactionState() == LocalTransactionState.ROLLBACK_MESSAGE) return false;
        else if (sendResult.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE) return true;

        return false;
    }

    //异步发送 购买成功通知 邮件
    public void asyncSendBuySuccessEmail(String orderId, String userName, String itemTitle, String userEmail) {

        Map<String, Object> bodyMap = new HashMap<>();

        final String sendSubject = buySuccessSubject;
        final String sendContent = String.format(buySuccessSubject, userName, itemTitle, orderId);

        bodyMap.put("sendSubject", sendSubject);
        bodyMap.put("sendContent", sendContent);
        bodyMap.put("userEmail", userEmail);
        System.out.println("生产者：邮件发送");
        Message message = new Message(emailTopic, JSON.toJSON(bodyMap).toString().getBytes(Charset.forName("utf-8")));
        try {
            producer.send(message);
        } catch (MQClientException e) {
            e.printStackTrace();
        } catch (RemotingException e) {
            e.printStackTrace();
        } catch (MQBrokerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
