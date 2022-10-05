package com.atguigu.gmall.list.receiver;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.list.service.SearchService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Aiden
 * @create 2022-09-30 16:33
 */
@Component
public class ListReceiver {

    @Autowired
    private SearchService searchService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_UPPER, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS, durable = "true", autoDelete = "false"),
            key = {MqConst.ROUTING_GOODS_UPPER}
    ))
    @SneakyThrows
    public void upperGoods(Long skuId, Message message, Channel channel) {

        try {
            if (skuId != null) {
                //调用本地方法
                searchService.upperGoods(skuId);
            }
        } catch (Exception e) {
            //出错后可以写入日志  或者发送短信给程序员，具体操作看情况在这里编写
            e.printStackTrace();
        }
                                       //拿到消息确认的唯一标识Tag     是否批量确认 false 确认一个消息，true 批量确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_LOWER, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS, durable = "true", autoDelete = "false"),
            key = {MqConst.ROUTING_GOODS_LOWER}
    ))
    @SneakyThrows
    public void lowerGoods(Long skuId, Message message, Channel channel) {

        try {
            if (skuId != null) {
                //调用本地方法
                searchService.lowerGoods(skuId);
            }
        } catch (Exception e) {
            //出错后可以写入日志  或者发送短信给程序员，具体操作看情况在这里编写
            e.printStackTrace();
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


}
