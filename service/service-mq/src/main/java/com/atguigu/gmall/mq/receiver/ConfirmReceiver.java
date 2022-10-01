package com.atguigu.gmall.mq.receiver;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author Aiden
 * @create 2022-09-30 15:15
 */
@Component
public class ConfirmReceiver {

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "exchange.confirm",durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = "exchange.confirm",durable = "true",autoDelete = "false"),
            key = {"routing.confirm"}
    ))
    @SneakyThrows               //amqp.core.Message  rabbitmq.client.Channel
    public void process(String msg, Message message, Channel channel){

        //是否批量确认 false 确认一个消息，true 批量确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
