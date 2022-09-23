package com.atguigu.gmall.list.service;

/**
 * @author Aiden
 * @create 2022-09-23 10:12
 */
public interface SearchService {

    void upperGoods(Long skuId);

    void lowerGoods(Long skuId);

    void incrHotScore(Long skuId);

}
