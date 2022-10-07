package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

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

    PaymentInfo getPaymentInfo(String outTradeNo, PaymentType alipay);

    void paySuccess(String outTradeNo, PaymentType alipay, Map<String, String> paramsMap);

    void updatePaymentInfo(String outTradeNo, String name, PaymentInfo paymentInfo);

    /**
     * 关闭支付记录
     * @param orderId
     */
    void closePayment(Long orderId);
}
