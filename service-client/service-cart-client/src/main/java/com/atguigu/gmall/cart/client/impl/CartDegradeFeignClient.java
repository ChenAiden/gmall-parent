package com.atguigu.gmall.cart.client.impl;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Aiden
 * @create 2022-09-29 10:06
 */
@Component
public class CartDegradeFeignClient implements CartFeignClient {


    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        return null;
    }
}
