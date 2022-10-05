package com.atguigu.gmall.order.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.stereotype.Component;

/**
 * @author Aiden
 * @create 2022-09-29 10:03
 */
@Component
public class OrderDegradeFeignClient implements OrderFeignClient {


    @Override
    public Result trade() {
        return Result.fail();
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        return null;
    }
}
