package com.atguigu.gmall.mq.receiver;

import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 死信队列实现延迟
 * @author Aiden
 * @create 2022-10-04 9:29
 */
@Component
public class DeadLetterReceiver {

    @SneakyThrows
    @RabbitListener(queues = DeadLetterMqConfig.queue_dead_2)
    public void getMsg(String msg, Message message, Channel channel){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:ss");

        System.out.println("我接收到的时间" + simpleDateFormat.format(new Date()));

        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }


}
