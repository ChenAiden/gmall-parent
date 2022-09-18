package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Aiden
 * @create 2022-09-15 22:22
 */
@Api("商品详情")
@RestController
@RequestMapping("api/item")
public class ItemApiController {

    @Autowired
    private ItemService itemService;


    //GET/api/item/{skuId} 详情接口
    @ApiOperation("详情接口")
    @GetMapping("/{skuId}")
    public Result<Map<String,Object>> getItem(@PathVariable Long skuId){

//        ItemVo itemVo = itemService.getSkuInfo(skuId);
        Map<String,Object> result = itemService.getBySkuInfo(skuId);
        return Result.ok(result);
    }



}
