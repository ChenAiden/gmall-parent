package com.atguigu.gmall.cart.client;

import com.atguigu.gmall.cart.client.impl.CartDegradeFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author Aiden
 * @create 2022-09-29 10:05
 */
@FeignClient(value = "service-cart",fallback = CartDegradeFeignClient.class)
public interface CartFeignClient {

    //GET/api/cart/getCartCheckedList/{userId}  获取选中状态的购物车列表
    @ApiOperation("获取选中状态的购物车列表")
    @GetMapping("/api/cart/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable String userId);
}
