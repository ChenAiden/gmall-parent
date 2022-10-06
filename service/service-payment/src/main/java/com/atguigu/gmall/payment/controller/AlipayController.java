package com.atguigu.gmall.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Aiden
 * @create 2022-10-04 16:25
 */
@Controller
//@RestController
@RequestMapping("/api/payment/alipay")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 获取支付页面二维码
     * 支付页面的选择支付宝
     * http://api.gmall.com/api/payment/alipay/submit/{orderId}(orderId=${orderInfo.id})
     *
     * @param orderId
     * @return
     */
    @ResponseBody
    @GetMapping("/submit/{orderId}")
    public String submit(@PathVariable Long orderId) {

        return alipayService.submit(orderId);
    }


    /**
     * 同步回调处理
     *
     * @return
     */
    @RequestMapping("/callback/return")
    public String callbackReturn() {
        //不能在这里（同步回调）做处理，因为有的用户可能不等待回调完成就退出

        return "redirect:" + AlipayConfig.return_order_url;
    }


    /**
     * 异步回调
     * <p>
     * 作用：接收支付宝处理结果，更订单状态，更新支付记录状态，扣减库存
     *
     * @param paramsMap
     * @return
     */
    @ResponseBody
    @RequestMapping("/callback/notify")
    public String callbackNotify(@RequestParam Map<String, String> paramsMap) {

        System.out.println("异步回调来了");

        //已经在参数中接收了
//        Map<String, String> paramsMap = ... //将异步通知中收到的所有参数都存放到map中

        //实现验签
        boolean signVerified = false; //调用SDK验证签名
        try {
            signVerified = AlipaySignature.rsaCheckV1(paramsMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        //获取订单交易号
        String outTradeNo = paramsMap.get("out_trade_no");
        //查询支付记录
        PaymentInfo paymentInfo = paymentService.getPaymentInfo(outTradeNo, PaymentType.ALIPAY);
        //获取金额
        String totalAmount = paramsMap.get("total_amount");
        //获取appId
        String appId = paramsMap.get("app_id");

        //获取订单状态
        String tradeStatus = paramsMap.get("trade_status");

        //获取notify_id
        //  保证异步通知的幂等性！notify_id
        String notifyId = paramsMap.get("notify_id");


        if (signVerified) {
            //  验签成功后，按照支付结果异步通知中的描述，
            //  对支付结果中的业务内容进行二次校验，
            //  校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            if (paymentInfo == null || new BigDecimal("0.01").compareTo(new BigDecimal(totalAmount)) != 0 || !AlipayConfig.app_id.equals(appId)) {
                return "failure";
            }


            //通过setnx 保证消息幂等性，如果订单
            Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(notifyId, notifyId, 1462, TimeUnit.MINUTES);

            if (!aBoolean){
                //存入失败了，说明已经发起过异步回调了，但是修改pay订单失败了，需要支付宝继续发起异步回调
                return "failure";
            }


            //判断订单状态（订单成功/完成才能算通过，否则都不发送success，这样支付宝才会继续发送异步回调）
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                //更新订单状态
                paymentService.paySuccess(outTradeNo, PaymentType.ALIPAY, paramsMap);

                return "success";
            }

        } else {
            //  验签失败则记录异常日志，并在response中返回failure.
            return "failure";
        }
        return "failure";
    }


    /**
     * 退款
     *
     * @param orderId
     * @return
     */
    @ResponseBody
    @GetMapping("/refund/{orderId}")
    public Result refund(@PathVariable Long orderId) {

        boolean refund = alipayService.refund(orderId);

        return Result.ok(refund);
    }


}
