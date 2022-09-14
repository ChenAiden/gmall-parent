package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuInfo;
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
 * @create 2022-09-12 20:06
 */
@Api(tags = "专属操作spu")
@RestController
@RequestMapping("admin/product")
public class SpuManageController {

    @Autowired
    private ManageService manageService;

    /**
     * spu分页列表
     * admin/product/{page}/{limit}
     *
     * @param page
     * @param limit
     * @param spuInfo
     * @return mybatisplus分页插件的使用
     * 1.导入依赖
     * 2.添加配置类
     * 3.使用IPage
     */
    @ApiOperation("spu分页列表")
    @GetMapping("/{page}/{limit}")
    public Result<IPage<SpuInfo>> getSpuInfoPage(@PathVariable Long page,
                                                 @PathVariable Long limit,
                                                 SpuInfo spuInfo) {
        Page<SpuInfo> spuInfoPage = new Page<>(page, limit);

        IPage<SpuInfo> spuInfoPageList = manageService.getSpuInfoPage(spuInfoPage, spuInfo);
        return Result.ok(spuInfoPageList);
    }

    //GET/admin/product/baseSaleAttrList 获取销售属性数据
    @ApiOperation("获取销售属性数据")
    @GetMapping("/baseSaleAttrList")
    public Result<List<BaseSaleAttr>> baseSaleAttrList() {

        List<BaseSaleAttr> baseSaleAttrList = manageService.baseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }

    /**
     * 保存spu
     *
     * @param spuInfo
     * @return
     */
    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        // 调用服务层的保存方法
        manageService.saveSpuInfo(spuInfo);
        return Result.ok();
    }


}
