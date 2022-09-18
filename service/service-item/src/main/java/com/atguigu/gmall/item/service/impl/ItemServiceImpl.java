package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public Map<String, Object> getBySkuInfo(Long skuId) {

        Map<String, Object> resultMap = new HashMap<>();

        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        resultMap.put("skuInfo",skuInfo);

        if (skuInfo != null){
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            resultMap.put("categoryView",categoryView);
        }

        List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
        if (!CollectionUtils.isEmpty(attrList)){

            List<Map> mapList = attrList.stream().map(baseAttrInfo -> {
                //创建map封装数据
                Map<String,String> skuAttrMap = new HashMap<>();
                skuAttrMap.put("attrName",baseAttrInfo.getAttrName());
                skuAttrMap.put("attrValue",baseAttrInfo.getAttrValueList().get(0).getValueName());
                return skuAttrMap;
            }).collect(Collectors.toList());
            resultMap.put("skuAttrList",mapList);
        }

        BigDecimal price = productFeignClient.getSkuPrice(skuId);
        resultMap.put("price",price);

        Long spuId = skuInfo.getSpuId();
        List<SpuPoster> spuPosterList = productFeignClient.findSpuPosterBySpuId(spuId);
        resultMap.put("spuPosterList",spuPosterList);

        List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, spuId);
        resultMap.put("spuSaleAttrList",spuSaleAttrList);

        Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(spuId);
        String valuesSkuJson = JSONObject.toJSONString(skuValueIdsMap);
        resultMap.put("valuesSkuJson",valuesSkuJson);

        return resultMap;
    }
}
