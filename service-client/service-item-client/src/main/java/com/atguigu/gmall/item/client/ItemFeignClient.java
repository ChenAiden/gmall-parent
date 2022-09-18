package com.atguigu.gmall.item.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.impl.ItemDegradeFeignClient;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author Aiden
 * @create 2022-09-17 11:24
 */
@FeignClient(value = "service-item",fallback = ItemDegradeFeignClient.class)
public interface ItemFeignClient {


    //GET/api/item/{skuId} 详情接口
    @ApiOperation("详情接口")
    @GetMapping("/api/item/{skuId}")
    public Result<Map<String,Object>> getItem(@PathVariable Long skuId);
}
