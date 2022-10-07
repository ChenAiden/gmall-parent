package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @author Aiden
 * @create 2022-10-04 15:58
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitService rabbitService;


    /**
     * 保存支付记录
     *
     * @param orderInfo
     * @param paymentType
     */
    @Override
    public void savePayment(OrderInfo orderInfo, PaymentType paymentType) {
        //判断是否已有支付记录
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderInfo.getId());
        wrapper.eq("payment_type", paymentType.name());

        Integer count = paymentInfoMapper.selectCount(wrapper);

        if (count > 0) {
            return;
        }
        PaymentInfo paymentInfo = new PaymentInfo();

        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setUserId(orderInfo.getUserId());
        paymentInfo.setPaymentType(paymentType.name());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(orderInfo.getTradeBody());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());

        paymentInfoMapper.insert(paymentInfo);
    }

    @Override
    public PaymentInfo getPaymentInfo(String outTradeNo, PaymentType alipay) {

        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no", outTradeNo);
        wrapper.eq("payment_type", alipay.name());

        return paymentInfoMapper.selectOne(wrapper);
    }

    /**
     * 支付成功修改，支付订单
     * @param outTradeNo
     * @param alipay
     * @param paramsMap
     */
    @Override
    public void paySuccess(String outTradeNo, PaymentType alipay, Map<String, String> paramsMap) {
        try {
            //查询是否有记录
            PaymentInfo paymentInfoQuery = getPaymentInfo(outTradeNo, alipay);

            if (paymentInfoQuery == null) {
                return;
            }

            //修改信息
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setTradeNo(paramsMap.get("trade_no"));
            paymentInfo.setPaymentStatus(PaymentStatus.PAID.name());
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setCallbackContent(JSON.toJSONString(paramsMap));

            //抽取后可以这样用
            updatePaymentInfo(outTradeNo, alipay.name(), paymentInfo);


            //payment订单修改完毕，发送消息修改order订单
            rabbitService.sendMessage(
                    MqConst.EXCHANGE_DIRECT_PAYMENT_PAY,
                    MqConst.ROUTING_PAYMENT_PAY,
                    paymentInfoQuery.getOrderId());

        } catch (Exception e) {

            //如果修改信息时出错，删除redis中存储的数据，这样支付宝才可以继续发送信息，调用方法再次去修改
            redisTemplate.delete(paramsMap.get("notify_id"));
            e.printStackTrace();
        }
    }

    /**
     * 修改支付记录
     * @param outTradeNo
     * @param name
     * @param paymentInfo
     */
    public void updatePaymentInfo(String outTradeNo, String name, PaymentInfo paymentInfo) {

        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no", outTradeNo);
        wrapper.eq("payment_type", name);

        paymentInfoMapper.update(paymentInfo,wrapper);
    }


    /**
     * 关闭支付记录
     * @param orderId
     */
    @Override
    public void closePayment(Long orderId) {
        //查询是否存在支付记录
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id",String.valueOf(orderId));
        wrapper.eq("payment_type",PaymentType.ALIPAY);
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(wrapper);

        if (paymentInfo != null){
            paymentInfo.setPaymentStatus(PaymentStatus.CLOSED.name());

            paymentInfoMapper.updateById(paymentInfo);
        }


    }


}
