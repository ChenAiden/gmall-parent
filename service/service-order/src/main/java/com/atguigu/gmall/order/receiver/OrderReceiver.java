package com.atguigu.gmall.order.receiver;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Aiden
 * @create 2022-10-04 11:39
 */
@Component
public class OrderReceiver {

    @Autowired
    private OrderInfoService orderInfoService;

    @SneakyThrows
    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    public void cancelOrder(Long orderId, Message message, Channel channel) {


        try {
            //判断当前订单Id不能为空
            if (orderId != null) {

                //获取订单对象
                OrderInfo orderInfo = orderInfoService.getById(orderId);

                //判断支付状态，进度状态，不满足的都不消费消息，这就保证了消息的幂等性，只有未支付状态的订单才能消费消息，其他任何情况都不可以
                if (orderInfo != null && "UNPAID".equals(orderInfo.getOrderStatus()) && "UNPAID".equals(orderInfo.getProcessStatus())) {
                    orderInfoService.cancelOrder(orderId);
                }
            }
        } catch (Exception e) {
            //消息没有正常被消费者处理：记录日志后续跟踪处理
            e.printStackTrace();
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
