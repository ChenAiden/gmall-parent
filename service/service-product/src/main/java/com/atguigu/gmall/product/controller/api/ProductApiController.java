package com.atguigu.gmall.product.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Aiden
 * @create 2022-09-15 23:06
 */
@Api("商品SPU/SKU远程调用接口")
@RestController
@RequestMapping("api/product")
public class ProductApiController {

    @Autowired
    private ManageService manageService;

    //GET/api/product/getBaseCategoryList 获取首页分类数据
    @ApiOperation("获取首页分类数据")
    @GetMapping("/getBaseCategoryList")
    public Result getBaseCategoryList(){
        List<JSONObject> list = manageService.getBaseCategoryList();

        return Result.ok(list);
    }


    /**
     * GET/api/product/inner/getSkuInfo/{skuId}  根据skuId获取skuInfo信息
     *
     * @param skuId
     * @return
     */
    @ApiOperation("根据skuId获取skuInfo信息")
    @GetMapping("/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId) {
        return manageService.getSkuInfo(skuId);
    }


    /**
     * GET/api/product/inner/getSkuPrice/{skuId}  根据skuId 获取最新的商品价格
     *
     * @param skuId
     * @return
     */
    @ApiOperation("根据skuId 获取最新的商品价格")
    @GetMapping("/inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId) {

        return manageService.getSkuPrice(skuId);
    }


    /**
     * GET/api/product/inner/findSpuPosterBySpuId/{spuId}  根据spuId 获取海报数据
     *
     * @param spuId
     * @return
     */
    @ApiOperation("根据spuId 获取海报数据")
    @GetMapping("/inner/findSpuPosterBySpuId/{spuId}")
    public List<SpuPoster> findSpuPosterBySpuId(@PathVariable Long spuId) {

        return manageService.findSpuPosterBySpuId(spuId);
    }


    /**
     * GET/api/product/inner/getCategoryView/{category3Id} 根据三级分类id获取分类信息
     *
     * @param category3Id
     * @return
     */
    @ApiOperation("根据三级分类id获取分类信息")
    @GetMapping("/inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id) {

        return manageService.getCategoryView(category3Id);
    }



    /**
     * GET/api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}  根据spuId,skuId 获取销售属性数据
     * @param skuId
     * @param spuId
     * @return
     */
    @ApiOperation("根据spuId,skuId 获取销售属性数据")
    @GetMapping("/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId,
                                                          @PathVariable Long spuId) {
        return manageService.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }


    //GET/api/product/inner/getSkuValueIdsMap/{spuId}  根据spuId 获取到销售属性值Id 与skuId 组成的数据集

    @ApiOperation("根据spuId 获取到销售属性值Id 与skuId 组成的数据集")
    @GetMapping("/inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable Long spuId) {

        return manageService.getSkuValueIdsMap(spuId);
    }


    /**
     * GET/api/product/inner/getAttrList/{skuId} 根据skuId 获取平台属性数据
     * @param skuId
     * @return
     */
    @ApiOperation("根据skuId 获取平台属性数据")
    @GetMapping("/inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable Long skuId){

        return manageService.getAttrList(skuId);
    }


    //根据sku_name模糊查询
    //GET/admin/activity/activityInfo/findSkuInfoByKeyword/{keyword}



}
