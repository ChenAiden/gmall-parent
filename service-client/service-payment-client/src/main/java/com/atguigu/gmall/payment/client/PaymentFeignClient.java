package com.atguigu.gmall.payment.client;

import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.client.impl.PaymentDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Aiden
 * @create 2022-10-06 10:23
 */
@FeignClient(value = "service-payment",fallback = PaymentDegradeFeignClient.class)
public interface PaymentFeignClient {

    /**
     * 根据交易编号查询支付记录
     *
     * @param outTradeNo
     * @return
     */
    @ResponseBody
    @GetMapping("/api/payment/alipay/getPaymentInfo/{outTradeNo}")
    public PaymentInfo getPaymentInfo(@PathVariable String outTradeNo);
    /**
     * 查询支付宝支付记录，是否未支付
     *
     * @param orderId
     * @return
     */
    @ResponseBody
    @GetMapping("/api/payment/alipay/checkPayment/{orderId}")
    public boolean checkPayment(@PathVariable Long orderId);


    /**
     * 查询支付宝支付记录，是否关闭
     *
     * @param orderId
     * @return
     */
    @ResponseBody
    @GetMapping("/api/payment/alipay/closePay/{orderId}")
    public boolean closePay(@PathVariable Long orderId);

}
