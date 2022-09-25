package com.atguigu.gmall.list.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.impl.ListDegradeFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Aiden
 * @create 2022-09-23 14:15
 */
@FeignClient(value = "service-list",fallback = ListDegradeFeignClient.class)
public interface ListFeignClient {

    @ApiOperation("更新商品的热度排名")
    @GetMapping("/api/list/inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable Long skuId);

    //GET/api/list/inner/lowerGoods/{skuId} 下架
    @ApiOperation("下架")
    @GetMapping("/api/list/inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable Long skuId);

    //GET/api/list/inner/upperGoods/{skuId} 上架
    @ApiOperation("上架")
    @GetMapping("/api/list/inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable Long skuId);

    //POST/api/list 商品搜索
    @ApiOperation("商品搜索")
    @PostMapping("/api/list")
    public Result search(@RequestBody SearchParam searchParam);



}
