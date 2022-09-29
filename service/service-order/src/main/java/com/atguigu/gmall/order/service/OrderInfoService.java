package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

/**
 * @author Aiden
 * @create 2022-09-29 11:31
 */
public interface OrderInfoService {

    Long submitOrder(OrderInfo orderInfo);

    String getTradeNo(String userId);

    boolean checkedTradeNo(String userId,String tradeNo);

    void deleteTradeNo(String userId);

    boolean checkStock(Long skuId, Integer skuNum);

}
