package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

/**
 * @author Aiden
 * @create 2022-09-27 10:18
 */
public interface CartService {

    void addToCart(String userId, Long skuId, Integer skuNum);

    List<CartInfo> cartList(String userId, String userTempId);

    void delectCart(String userId, String skuId);

    void checkCart(String userId, String skuId, Integer isChecked);

    void chooseAll(String userId, Integer isChooseAll);

    List<CartInfo> getCartCheckedList(String userId);
}
