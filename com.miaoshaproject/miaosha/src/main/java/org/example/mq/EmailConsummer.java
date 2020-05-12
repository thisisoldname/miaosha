package org.example.mq;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.example.dao.ItemStockDOMapper;
import org.example.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/***************************
 *Author:ct
 *Time:2020/4/21 22:17
 *Dec:Todo
 ****************************/
@Component
public class EmailConsummer {

    @Autowired
    ItemStockDOMapper itemStockDOMapper;

    private DefaultMQPushConsumer consumer;

    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topic.email}")
    private String emailTopic;

    @Autowired
    private EmailService emailService;

//    @Value("${email.item.buy.success.subject}")
//    private String buySuccessSubject;
//
//    @Value("${email.item.buy.success.content}")
//    private String uccessContent;

    @PostConstruct
    public void init() throws MQClientException {

        consumer = new DefaultMQPushConsumer("email_consumer_group");
        consumer.setNamesrvAddr(nameAddr);
        consumer.subscribe(emailTopic, "*");
        //发送消息失败重试次数
        consumer.setMaxReconsumeTimes(1);
        consumer.registerMessageListener(new MessageListenerConcurrently() {

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {

                Message message = list.get(0);
                String jsonString = new String(message.getBody());
                Map<String, Object> map = JSON.parseObject(jsonString, Map.class);
                String sendSubject = (String) map.get("sendSubject");
                String sendContent = (String) map.get("sendContent");
                String userEmail = (String) map.get("userEmail");
                //由于邮件经常发送失败异常，RocketMQ消费机制会一直重试消费消息，设置重试次数

                System.out.println("发送邮件中......"+userEmail);
                //发送邮件
                emailService.senHtmlEmail(new String[]{userEmail}, sendSubject, sendContent);
                System.out.println("发送邮件成功......"+userEmail);

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
    }
}
