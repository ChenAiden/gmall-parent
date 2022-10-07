package com.atguigu.gmall.order.receiver;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.payment.client.PaymentFeignClient;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author Aiden
 * @create 2022-10-04 11:39
 */
@Component
public class OrderReceiver {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private PaymentFeignClient paymentFeignClient;

    /**
     * 超时  订单关闭
     *
     * @param orderId
     * @param message
     * @param channel
     */
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

                    //查询支付记录
                    PaymentInfo paymentInfo = paymentFeignClient.getPaymentInfo(orderInfo.getOutTradeNo());
                    if (paymentInfo != null && paymentInfo.getPaymentStatus().equals(PaymentStatus.UNPAID.name())) {

                        //查询支付宝中是否有记录，只有处于等待支付时才返回true
                        boolean checkPayment = paymentFeignClient.checkPayment(orderId);
                        if (checkPayment) {

                            //判断是否需要关闭支付宝中的订单
                            boolean closePay = paymentFeignClient.closePay(orderId);
                            if (closePay) {
                                //关闭订单
                                orderInfoService.cancelOrder(orderId, "2");
                            } else {
                                //关闭支付宝失败，说明可能在最后操作过程中用户支付成功了
                                //用户支付了都会自动异步回调，按照异步回调的过程修改状态即可
                            }

                        } else {
                            //只打开了支付页面（生成了本地支付表），没有支付,关闭本地支付表
                            orderInfoService.cancelOrder(orderId, "2");
                        }

                    } else {
                        //关闭订单
                        orderInfoService.cancelOrder(orderId, "1");
                    }
                }
            }
        } catch (Exception e) {
            //消息没有正常被消费者处理：记录日志后续跟踪处理
            e.printStackTrace();
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


    /**
     * 支付成功后修改订单状态，并发送消息扣减库存
     *
     * @param orderId
     * @param message
     * @param channel
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_PAYMENT_PAY, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY, durable = "true", autoDelete = "false"),
            key = {MqConst.ROUTING_PAYMENT_PAY}
    ))
    public void paySuccessUpdateOrder(Long orderId, Message message, Channel channel) {

        try {
            if (orderId != null) {
                orderInfoService.updateOrder(orderId, ProcessStatus.PAID);

                //发送消息修改库存
                orderInfoService.sendOrderSatusToSotck(orderId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


    /**
     * 接收库存消息，根据库存消息更改状态
     *
     * @param mapJson
     * @param message
     * @param channel
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_WARE_ORDER, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_WARE_ORDER),
            key = {MqConst.ROUTING_WARE_ORDER}
    ))
    public void lockStock(String mapJson, Message message, Channel channel) {

        if (!StringUtils.isEmpty(mapJson)) {
            Map<String, String> map = JSON.parseObject(mapJson, Map.class);

            String orderId = map.get("orderId");
            if ("DEDUCTED".equals(map.get("status"))) {
                //扣减库存成功
                //修改订单,状态为待发货
                orderInfoService.updateOrder(Long.valueOf(orderId), ProcessStatus.WAITING_DELEVER);

                //应该再去对接物流系统
            } else {
                //超卖了
                //修改订单，状态为库存异常
                orderInfoService.updateOrder(Long.valueOf(orderId), ProcessStatus.STOCK_EXCEPTION);
            }
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


}
