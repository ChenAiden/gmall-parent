package com.atguigu.gmall.list.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.stereotype.Component;

/**
 * @author Aiden
 * @create 2022-09-23 14:17
 */
@Component
public class ListDegradeFeignClient implements ListFeignClient {

    @Override
    public Result incrHotScore(Long skuId) {
        return Result.fail();
    }

    @Override
    public Result lowerGoods(Long skuId) {
        return Result.fail();
    }

    @Override
    public Result upperGoods(Long skuId) {
        return Result.fail();
    }

    @Override
    public Result search(SearchParam searchParam) {
        return Result.fail();
    }

}
