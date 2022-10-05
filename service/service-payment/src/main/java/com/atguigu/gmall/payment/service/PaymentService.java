package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;

/**
 * @author Aiden
 * @create 2022-10-04 15:57
 */
public interface PaymentService {

    /**
     * 保存支付记录
     * @param orderInfo
     * @param paymentType
     */
    void savePayment(OrderInfo orderInfo, PaymentType paymentType);
}
