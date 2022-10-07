package com.atguigu.gmall.payment.client.impl;

import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.client.PaymentFeignClient;
import org.springframework.stereotype.Component;

/**
 * @author Aiden
 * @create 2022-10-06 10:23
 */
@Component
public class PaymentDegradeFeignClient implements PaymentFeignClient {

    @Override
    public PaymentInfo getPaymentInfo(String outTradeNo) {
        return null;
    }

    @Override
    public boolean checkPayment(Long orderId) {
        return false;
    }

    @Override
    public boolean closePay(Long orderId) {
        return false;
    }
}
