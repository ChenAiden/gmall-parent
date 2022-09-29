package com.atguigu.gmall.order.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.order.client.impl.OrderDegradeFeignClient;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

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

}
