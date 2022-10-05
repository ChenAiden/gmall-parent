package com.atguigu.gmall.weball.controller;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Aiden
 * @create 2022-10-04 15:43
 */
@Controller
@SuppressWarnings("all")
public class PaymentController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @GetMapping("/pay.html")
    public String pay(Long orderId, Model model) {

        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);

        model.addAttribute("orderInfo", orderInfo);

        return "payment/pay";
    }


    /**
     * 支付成功页
     *
     * @return
     */
    @GetMapping("/pay/success.html")
    public String success() {
        return "payment/success";
    }


}
