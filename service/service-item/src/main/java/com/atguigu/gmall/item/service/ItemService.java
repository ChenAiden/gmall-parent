package com.atguigu.gmall.item.service;

import java.util.Map;

/**
 * @author Aiden
 * @create 2022-09-15 22:21
 */
public interface ItemService {

    Map<String, Object> getBySkuInfo(Long skuId);
}
