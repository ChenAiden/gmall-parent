package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Aiden
 * @create 2022-09-23 9:42
 */
@RestController
@RequestMapping("/api/list")
public class ListApiController {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private SearchService searchService;

    //GET/api/list/createIndex
    @ApiOperation("创建库，并依据库中注解创建索引")
    @GetMapping("/createIndex")
    public Result createIndex(){
        //创建索引库
        restTemplate.createIndex(Goods.class);
        //创建
        restTemplate.putMapping(Goods.class);

        return Result.ok();
    }

    //GET/api/list/inner/upperGoods/{skuId} 上架
    @ApiOperation("上架")
    @GetMapping("/inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable Long skuId){

        searchService.upperGoods(skuId);
        return Result.ok();
    }

    //GET/api/list/inner/lowerGoods/{skuId} 下架
    @ApiOperation("下架")
    @GetMapping("/inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable Long skuId){

//        restTemplate.delete(String.valueOf(skuId),Goods.class);
        searchService.lowerGoods(skuId);
        return Result.ok();
    }


    /**
     * GET/api/list/inner/incrHotScore/{skuId}  更新商品的热度排名
     * @param skuId
     * @return
     */
    @ApiOperation("更新商品的热度排名")
    @GetMapping("/inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable Long skuId){

        searchService.incrHotScore(skuId);
        return Result.ok();
    }


    //POST/api/list 商品搜索



}
