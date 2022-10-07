package com.atguigu.gmall.order.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.impl.OrderDegradeFeignClient;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Aiden
 * @create 2022-09-29 10:01
 */
@FeignClient(value = "service-order", fallback = OrderDegradeFeignClient.class)
public interface OrderFeignClient {

    //GET/api/order/auth/trade  去结算
    @ApiOperation("去结算")
    @GetMapping("/api/order/auth/trade")
    public Result trade();



    @GetMapping("/api/order/inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId);


    /**
     * 秒杀提交订单,不需要做校验了
     *
     * /api/order/inner/seckill/submitOrder
     * @param orderInfo
     * @return
     */
    @ApiOperation("秒杀提交订单")
    @PostMapping("/api/order/inner/seckill/submitOrder")
    public Long submitOrder(@RequestBody OrderInfo orderInfo);


}
