package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Aiden
 * @create 2022-09-14 13:37
 */
@Api(tags = "商品SKU接口")
@RestController
@RequestMapping("/admin/product")
public class SkuManageController {

    @Autowired
    private ManageService manageService;

    /**
     * GET/admin/product/spuImageList/{spuId}  根据spuId 获取spuImage 集合
     * @param spuId
     * @return
     */
    @ApiOperation("根据spuId 获取spuImage 集合")
    @GetMapping("/spuImageList/{spuId}")
    public Result<List<SpuImage>> spuImageList(@PathVariable Long spuId){

        List<SpuImage> spuImageList = manageService.getSpuImageList(spuId);
        return Result.ok(spuImageList);
    }


    /**
     * GET/admin/product/spuSaleAttrList/{spuId}  根据spuId 查询销售属性
     * @param spuId
     * @return
     */
    @ApiOperation("根据spuId 查询销售属性")
    @GetMapping("/spuSaleAttrList/{spuId}")
    public Result<List<SpuSaleAttr>> spuSaleAttrList(@PathVariable Long spuId){

        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrList);
    }


    /**
     * POST/admin/product/saveSkuInfo  保存SkuInfo
     * @param skuInfo
     * @return
     */
    @ApiOperation("保存SkuInfo")
    @PostMapping("/saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){

        manageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }


    /**
     * sku分页列表
     * GET/admin/product/list/{page}/{limit}
     *
     * @param page
     * @param limit
     * @param spuInfo
     * @return mybatisplus分页插件的使用
     * 1.导入依赖
     * 2.添加配置类
     * 3.使用IPage
     */
    @ApiOperation("sku分页列表")
    @GetMapping("/list/{page}/{limit}")
    public Result<IPage<SkuInfo>> getSkuInfoPage(@PathVariable Long page,
                                                 @PathVariable Long limit,
                                                 SkuInfo spuInfo) {
        Page<SkuInfo> skuInfoPage = new Page<>(page, limit);

        IPage<SkuInfo> skuInfoIPage = manageService.getSkuInfoPage(skuInfoPage, spuInfo);
        return Result.ok(skuInfoIPage);
    }

    /**
     * GET/admin/product/cancelSale/{skuId} 下架
     *
     * @param skuId
     * @return
     */
    @ApiOperation("下架")
    @GetMapping("/cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId){

        manageService.cancelSale(skuId);
        return Result.ok();
    }


    /**
     *  GET/admin/product/onSale/{skuId} 上架
     *
     * @param skuId
     * @return
     */
    @ApiOperation("上架")
    @GetMapping("/onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId){

        manageService.onSale(skuId);
        return Result.ok();
    }
}
