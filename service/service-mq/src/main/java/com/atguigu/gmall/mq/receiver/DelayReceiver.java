package com.atguigu.gmall.mq.receiver;

import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 延迟插件接收死信队列消息
 *
 * @author Aiden
 * @create 2022-10-04 10:23
 */
@Component
public class DelayReceiver {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 使用redis的setnx保证消息幂等性
     *
     * @param msg
     * @param message
     * @param channel
     */
    @SneakyThrows
    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
    public void get(String msg, Message message, Channel channel) {

        String key = "delay:" + msg;

        //存储消息到redis
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(key, "0", 10, TimeUnit.MINUTES);

        if (!aBoolean) {
            //设置失败说明不是第一次接收消息
            //获取redis中的数据，检查是否已经成功消费
            String flag = (String) redisTemplate.opsForValue().get(key);

            if ("1".equals(flag)) {
                //等于1说明消息已经被消费了，可能没有确认，再次确认消息
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            } else {

                //等于0说明之前存了redis但是没有消费，并且没有确认
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.out.println("Receive queue_delay_1: " + sdf.format(new Date()) + " Delay rece." + msg);

                //  修改redis 中的数据
                this.redisTemplate.opsForValue().set(key, "1");
                //  手动确认消息
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }
        }

        //走到这里说明之前设置成功了，消息消费了，但是还到了这里说明没有确认消息


        //设置成功，说明是第一次接收消息
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("Receive queue_delay_1: " + sdf.format(new Date()) + " Delay rece." + msg);

        //  修改redis 中的数据
        this.redisTemplate.opsForValue().set(key, "1");
        //  手动确认消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


    //普通队列的接收方式，拿到后打印一下，然后手动确认
//    @SneakyThrows
//    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
//    public void get(String msg, Message message, Channel channel) {
//
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        System.out.println("Receive queue_delay_1: " + sdf.format(new Date()) + " Delay rece." + msg);
//
//
//        //  手动确认消息
//        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//    }

}
