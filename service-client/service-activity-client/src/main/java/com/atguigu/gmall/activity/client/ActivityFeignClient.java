package com.atguigu.gmall.activity.client;

import com.atguigu.gmall.activity.client.impl.ActivityDegradeFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Aiden
 * @create 2022-10-06 14:18
 */
@FeignClient(value = "service-activity",fallback = ActivityDegradeFeignClient.class)
public interface ActivityFeignClient {

    @GetMapping("/api/activity/seckill/findAll")
    public Result findAll();


    /**
     * 查询具体商品
     * GET/api/activity/seckill/getSeckillGoods/{skuId}
     * @param skuId
     * @return
     */
    @GetMapping("/api/activity/seckill/getSeckillGoods/{skuId}")
    public Result getSeckillGoods(@PathVariable String skuId);


    /**
     * GET/api/activity/seckill/auth/trade  秒杀下单确认
     * @return
     */
    @GetMapping("/api/activity/seckill/auth/trade")
    public Result trade();

}
