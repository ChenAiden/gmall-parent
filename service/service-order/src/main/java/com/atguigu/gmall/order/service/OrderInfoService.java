package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

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

    IPage<OrderInfo> getOrderByPage(Page<OrderInfo> orderInfoPage, String userId);

}
