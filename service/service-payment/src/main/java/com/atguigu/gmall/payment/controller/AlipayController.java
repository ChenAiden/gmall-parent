package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

        String from = alipayService.submit(orderId);

        return from;
    }


    /**
     * 同步回调处理
     *
     * @return
     */
    @RequestMapping("/callback/return")
    public String callbackReturn() {
        return "redirect:" + AlipayConfig.return_order_url;
    }
}
