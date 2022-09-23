package com.atguigu.gmall.list.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.impl.ListDegradeFeignClient;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Aiden
 * @create 2022-09-23 14:15
 */
@FeignClient(value = "service-list",fallback = ListDegradeFeignClient.class)
public interface ListFeignClient {

    @ApiOperation("更新商品的热度排名")
    @GetMapping("/api/list/inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable Long skuId);


}
