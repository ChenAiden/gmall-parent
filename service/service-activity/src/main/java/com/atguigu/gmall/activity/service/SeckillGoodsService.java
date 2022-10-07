package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.List;

/**
 * @author Aiden
 * @create 2022-10-06 16:25
 */
public interface SeckillGoodsService {

    List<SeckillGoods> findAll();

    SeckillGoods getSeckillGoods(String skuId);

    void seckillUser(String userId,Long skuId);

    Result checkOrder(String userId, Long skuId);

}
