package com.atguigu.gmall.product.client;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.impl.ProductDegradeFeignClient;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Aiden
 * @create 2022-09-17 10:45
 */
@FeignClient(value = "service-product",fallback = ProductDegradeFeignClient.class)
public interface ProductFeignClient {


    /**
     * GET/api/product/inner/getSkuInfo/{skuId}  根据skuId获取skuInfo信息
     *
     * @param skuId
     * @return
     */
    @ApiOperation("根据skuId获取skuInfo信息")
    @GetMapping("/api/product/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId);


    /**
     * GET/api/product/inner/getSkuPrice/{skuId}  根据skuId 获取最新的商品价格
     *
     * @param skuId
     * @return
     */
    @ApiOperation("根据skuId 获取最新的商品价格")
    @GetMapping("/api/product/inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId);

    /**
     * GET/api/product/inner/findSpuPosterBySpuId/{spuId}  根据spuId 获取海报数据
     *
     * @param spuId
     * @return
     */
    @ApiOperation("根据spuId 获取海报数据")
    @GetMapping("/api/product/inner/findSpuPosterBySpuId/{spuId}")
    public List<SpuPoster> findSpuPosterBySpuId(@PathVariable Long spuId);


    /**
     * GET/api/product/inner/getCategoryView/{category3Id} 根据三级分类id获取分类信息
     *
     * @param category3Id
     * @return
     */
    @ApiOperation("根据三级分类id获取分类信息")
    @GetMapping("/api/product/inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id);



    /**
     * GET/api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}  根据spuId,skuId 获取销售属性数据
     * @param skuId
     * @param spuId
     * @return
     */
    @ApiOperation("根据spuId,skuId 获取销售属性数据")
    @GetMapping("/api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId,
                                                          @PathVariable Long spuId);



    //GET/api/product/inner/getSkuValueIdsMap/{spuId}  根据spuId 获取到销售属性值Id 与skuId 组成的数据集

    @ApiOperation("根据spuId 获取到销售属性值Id 与skuId 组成的数据集")
    @GetMapping("/api/product/inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable Long spuId);



    /**
     * GET/api/product/inner/getAttrList/{skuId} 根据skuId 获取平台属性数据
     * @param skuId
     * @return
     */
    @ApiOperation("根据skuId 获取平台属性数据")
    @GetMapping("/api/product/inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable Long skuId);


}
