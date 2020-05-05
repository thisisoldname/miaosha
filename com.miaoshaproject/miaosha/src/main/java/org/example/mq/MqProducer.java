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

    @Value("${mq.topicname}")
    private String topicName;

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
                try {
                    orderService.createOrder(userId, itemId, promoId, amount, stockLogId);
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

                if(stockLogDO.getStatus().intValue() == 2) return LocalTransactionState.COMMIT_MESSAGE;
                else if(stockLogDO.getStatus().intValue() == 1) return LocalTransactionState.UNKNOW;
                else return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        });
    }

    //同步库存扣减消息
    public boolean asyncReduceStock(Integer itemId, Integer amount) {

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        Message message = new Message(topicName, "increase", JSON.toJSON(bodyMap).toString().getBytes(Charset.forName("utf-8")));

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
    public boolean transationAsynReduceStock(Integer userId, Integer promoId, Integer itemId, Integer amount, String stockLogId) {

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
        argsMap.put("stockLogId", stockLogId);
        Message message = new Message(topicName, "increase", JSON.toJSON(bodyMap).toString().getBytes(Charset.forName("utf-8")));

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
}
