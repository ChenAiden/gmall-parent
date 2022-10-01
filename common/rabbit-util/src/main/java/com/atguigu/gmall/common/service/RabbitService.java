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

    /**
     *  发送消息
     * @param exchange 交换机
     * @param routingKey 路由键
     * @param message 消息
     */
    public boolean sendMessage(String exchange, String routingKey, Object message) {

        //封装消息
        GmallCorrelationData correlationData = new GmallCorrelationData();

        //声明一个correlationId的变量
        String id = UUID.randomUUID().toString().replace("-","");
        //消息体对象
        correlationData.setId(id);
        correlationData.setMessage(message);
        correlationData.setExchange(exchange);
        correlationData.setRoutingKey(routingKey);

        //更新redis    发送消息的时候，将这个gmallCorrelationData 对象放入缓存
        redisTemplate.opsForValue().set(id, JSON.toJSONString(correlationData),10, TimeUnit.MINUTES);


        //发送消息携带数据对象   和一个消息实体对象
        rabbitTemplate.convertAndSend(exchange, routingKey, message,correlationData);

        return true;
    }


}
