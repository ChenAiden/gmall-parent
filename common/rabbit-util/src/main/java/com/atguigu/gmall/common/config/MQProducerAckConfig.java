package com.atguigu.gmall.common.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.model.GmallCorrelationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Aiden
 * @create 2022-09-30 15:00
 *
 * @Description 消息发送确认
 * <p>
 * ConfirmCallback  只确认消息是否正确到达 Exchange 中
 * ReturnCallback   消息没有正确到达队列时触发回调，如果正确到达队列不执行
 * <p>
 * 1. 如果消息没有到exchange,则confirm回调,ack=false
 * 2. 如果消息到达exchange,则confirm回调,ack=true
 * 3. exchange到queue成功,则不回调return
 * 4. exchange到queue失败,则回调return
 *
 */
@Component
@Slf4j
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;


    // 修饰一个非静态的void（）方法,在服务器加载Servlet的时候运行，
    // 并且只会被服务器执行一次在构造函数之后执行，init（）方法之前执行。

    //在容器生成之后将此配置添加
    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);            //指定 ConfirmCallback
        rabbitTemplate.setReturnCallback(this);             //指定 ReturnCallback
    }


    //发送到交换机时  成功或失败  都执行此方法
    @Override                           //消息的具体信息    是否成功     失败的原因
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            log.info("消息发送成功：" + JSON.toJSONString(correlationData));
        } else {
            log.info("消息发送失败：" + cause + " 数据：" + JSON.toJSONString(correlationData));

            //在此方法中尝试重试
            this.retryMsg(correlationData);
        }
    }



    //消息发送失败时，在此方法中尝试重试
    private void retryMsg(CorrelationData correlationData) {

        //强转成我们的消息体对象
        GmallCorrelationData gmallCorrelationData = (GmallCorrelationData) correlationData;

        //获取重试次数
        int retryCount = gmallCorrelationData.getRetryCount();

        if (retryCount > 3){

        }else {
            //更新对象数据
            gmallCorrelationData.setRetryCount(++retryCount);
            //更新redis
            redisTemplate.opsForValue().set(gmallCorrelationData.getId(),JSON.toJSONString(gmallCorrelationData));

            //再次发送消息
            rabbitTemplate.convertAndSend(
                    gmallCorrelationData.getExchange(),
                    gmallCorrelationData.getRoutingKey(),
                    gmallCorrelationData.getMessage(),
                    gmallCorrelationData);
        }
    }



    //发送到队列时  失败  执行此方法
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        // 反序列化对象输出
        System.out.println("消息主体: " + new String(message.getBody()));
        System.out.println("应答码: " + replyCode);
        System.out.println("描述：" + replyText);
        System.out.println("消息使用的交换器 exchange : " + exchange);
        System.out.println("消息使用的路由键 routing : " + routingKey);

        //获取数据对象的id
        String correlationId = (String) message.getMessageProperties().getHeaders().get("spring_returned_message_correlation");

        //从redis中获取信息
        String str = (String) redisTemplate.opsForValue().get(correlationId);
        //转化为消息实体
        GmallCorrelationData gmallCorrelationData = JSON.parseObject(str, GmallCorrelationData.class);

        //重试
        this.retryMsg(gmallCorrelationData);
    }

}
