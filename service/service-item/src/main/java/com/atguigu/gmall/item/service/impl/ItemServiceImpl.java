package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author Aiden
 * @create 2022-09-15 22:21
 */
@Service
//@SuppressWarnings(value={"unchecked", "rawtypes"})
public class ItemServiceImpl implements ItemService {


    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public Map<String, Object> getBySkuInfo(Long skuId) {

        Map<String, Object> resultMap = new HashMap<>();


//        //判断布隆过滤器中是否有这个skuid
//        RBloomFilter<Long> rBloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
//
//        if (!rBloomFilter.contains(skuId)) return resultMap;

        //异步编排
        CompletableFuture<SkuInfo> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            resultMap.put("skuInfo", skuInfo);
            return skuInfo;
        }, executor);

        CompletableFuture<Void> categoryViewFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            if (skuInfo != null) {
                BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
                resultMap.put("categoryView", categoryView);
            }
        }, executor);

        CompletableFuture<Void> skuAttrListFuture = CompletableFuture.runAsync(() -> {
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            if (!CollectionUtils.isEmpty(attrList)) {

                List<Map> mapList = attrList.stream().map(baseAttrInfo -> {
                    //创建map封装数据
                    Map<String, String> skuAttrMap = new HashMap<>();
                    skuAttrMap.put("attrName", baseAttrInfo.getAttrName());
                    skuAttrMap.put("attrValue", baseAttrInfo.getAttrValueList().get(0).getValueName());
                    return skuAttrMap;
                }).collect(Collectors.toList());
                resultMap.put("skuAttrList", mapList);
            }
        }, executor);

        CompletableFuture<Void> priceFuture = CompletableFuture.runAsync(() -> {
            BigDecimal price = productFeignClient.getSkuPrice(skuId);
            resultMap.put("price", price);
        }, executor);

        CompletableFuture<Void> spuPosterListFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            List<SpuPoster> spuPosterList = productFeignClient.findSpuPosterBySpuId(skuInfo.getSpuId());
            resultMap.put("spuPosterList", spuPosterList);
        }, executor);


        CompletableFuture<Void> spuSaleAttrListFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            resultMap.put("spuSaleAttrList", spuSaleAttrList);
        }, executor);


        CompletableFuture<Void> valuesSkuJsonFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            String valuesSkuJson = JSONObject.toJSONString(skuValueIdsMap);
            resultMap.put("valuesSkuJson", valuesSkuJson);
        }, executor);

        //多任务组合
        CompletableFuture.allOf(
                skuInfoFuture,
                categoryViewFuture,
                skuAttrListFuture,
                priceFuture,
                spuPosterListFuture,
                spuSaleAttrListFuture,
                valuesSkuJsonFuture
        ).join();

        return resultMap;
    }
}
