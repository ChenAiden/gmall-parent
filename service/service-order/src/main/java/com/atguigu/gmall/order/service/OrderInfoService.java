package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author Aiden
 * @create 2022-09-29 11:31
 */
public interface OrderInfoService extends IService<OrderInfo> {

    Long submitOrder(OrderInfo orderInfo);

    String getTradeNo(String userId);

    boolean checkedTradeNo(String userId,String tradeNo);

    void deleteTradeNo(String userId);

    boolean checkStock(Long skuId, Integer skuNum);

    IPage<OrderInfo> getOrderByPage(Page<OrderInfo> orderInfoPage, String userId);

    void cancelOrder(Long orderId);

    void updateOrder(Long orderId, ProcessStatus processStatus);

    OrderInfo getOrderInfoById(Long orderId);

    void sendOrderSatusToSotck(Long orderId);

    List<OrderInfo> orderSplit(String orderId, String wareSkuMap);

    Map<String, Object> getStringObjectMap(OrderInfo orderInfo);
}
