package com.atguigu.gmall.weball.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @author Aiden
 * @create 2022-09-29 9:18
 */
@Controller
@SuppressWarnings("all")
public class OrderController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @RequestMapping("/trade.html")
    public String trade(Model model){

        //获取数据
        Result<Map> trade = orderFeignClient.trade();
        /**
         * 在Feign的方法调用中会重新创建request，导致请求头信息丢失，我们在web-util中创建拦截器，为新的request中添加上原来的header
         */

        model.addAllAttributes(trade.getData());

        return "order/trade";
    }
}
