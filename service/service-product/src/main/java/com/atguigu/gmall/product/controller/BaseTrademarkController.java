package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Aiden
 * @create 2022-09-12 20:44
 */
@Api(tags = "品牌管理")
@RestController
@RequestMapping("/admin/product/baseTrademark")
public class BaseTrademarkController {


    @Autowired
    private BaseTrademarkService baseTrademarkService;


    /**
     * 品牌管理分页列表
     * /admin/product/baseTrademark/{page}/{limit}
     *
     * @param page
     * @param limit
     * @return
     */
    @ApiOperation("品牌管理分页列表")
    @GetMapping("/{page}/{limit}")
    public Result<IPage<BaseTrademark>> getBaseTrademarkPage(@PathVariable Long page,
                                                             @PathVariable Long limit) {
        Page<BaseTrademark> baseTrademarkPage = new Page<>(page, limit);
        IPage<BaseTrademark> baseTrademarkIPage = baseTrademarkService.getBaseTrademarkPage(baseTrademarkPage);
        return Result.ok(baseTrademarkIPage);
    }


    /**
     * 根据品牌Id 获取品牌对象
     * /admin/product/baseTrademark/get/{id}
     * @param id
     * @return
     */
    @ApiOperation("根据品牌Id 获取品牌对象")
    @GetMapping("get/{id}")
    public Result<BaseTrademark> getBaseTrademark(@PathVariable Long id) {
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);

        return Result.ok(baseTrademark);
    }


    /**
     * 保存
     * /admin/product/baseTrademark/save
     * @param baseTrademark
     * @return
     */
    @ApiOperation("新增BaseTrademark")
    @PostMapping("save")
    public Result save(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.save(baseTrademark);

        return Result.ok();
    }

    /**
     * 修改
     * PUT/admin/product/baseTrademark/update
     *
     * @param baseTrademark
     * @return
     */
    @ApiOperation("修改BaseTrademark")
    @PutMapping("update")
    public Result update(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.updateById(baseTrademark);

        return Result.ok();
    }


    /**删除
     * DELETE/admin/product/baseTrademark/remove/{id}
     * @param id
     * @return
     */
    @ApiOperation(value = "删除BaseTrademark")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        baseTrademarkService.removeById(id);

        return Result.ok();
    }

}
