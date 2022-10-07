package com.atguigu.gmall.activity.client.impl;

import com.atguigu.gmall.activity.client.ActivityFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.stereotype.Component;

/**
 * @author Aiden
 * @create 2022-10-06 14:19
 */
@Component
public class ActivityDegradeFeignClient implements ActivityFeignClient {

    @Override
    public Result findAll() {
        return Result.fail();
    }

    @Override
    public Result<SeckillGoods> getSeckillGoods(String skuId) {
        return Result.fail();
    }

    @Override
    public Result trade() {
        return Result.fail();
    }


}
