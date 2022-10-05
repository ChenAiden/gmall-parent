package com.atguigu.gmall.common.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.model.GmallCorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Aiden
 * @create 2022-09-30 15:07
 */
@Service
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;


    //使用延迟插件发送消息
    public boolean sendDelayMsg(String exchange, String routingKey, Object msg, int delayTime) {

        //封装消息
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();

        //声明一个correlationId的变量
        String id = UUID.randomUUID().toString().replace("-", "");
        //消息体对象
        gmallCorrelationData.setId(id);
        gmallCorrelationData.setMessage(msg);
        gmallCorrelationData.setExchange(exchange);
        gmallCorrelationData.setRoutingKey(routingKey);

        //设置为延迟消息
        gmallCorrelationData.setDelay(true);
        //设置延迟时间
        gmallCorrelationData.setDelayTime(delayTime);

        //更新redis    发送消息的时候，将这个gmallCorrelationData 对象放入缓存 ，发送队列失败时会需要
        redisTemplate.opsForValue().set(id, JSON.toJSONString(gmallCorrelationData), 10, TimeUnit.MINUTES);

        //发送延迟消息
        rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                msg,
                message -> {
                    message.getMessageProperties().setDelay(delayTime * 1000);
                    return message;
                },
                gmallCorrelationData);

        return true;
    }


    /**
     * 发送消息
     *
     * @param exchange   交换机
     * @param routingKey 路由键
     * @param message    消息
     */
    public boolean sendMessage(String exchange, String routingKey, Object message) {

        //封装消息
        GmallCorrelationData correlationData = new GmallCorrelationData();

        //声明一个correlationId的变量
        String id = UUID.randomUUID().toString().replace("-", "");
        //消息体对象
        correlationData.setId(id);
        correlationData.setMessage(message);
        correlationData.setExchange(exchange);
        correlationData.setRoutingKey(routingKey);

        //更新redis    发送消息的时候，将这个gmallCorrelationData 对象放入缓存 ，发送队列失败时会需要
        redisTemplate.opsForValue().set(id, JSON.toJSONString(correlationData), 10, TimeUnit.MINUTES);


        //发送消息携带数据对象   和一个消息实体对象
        rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                message,
                correlationData);

        return true;
    }


}
