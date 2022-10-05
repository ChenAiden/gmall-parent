package com.atguigu.gmall.payment.service.impl;

import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Aiden
 * @create 2022-10-04 15:58
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;


    /**
     * 保存支付记录
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


}
