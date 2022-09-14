package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Aiden
 * @create 2022-09-12 21:38
 */
@RestController
@RequestMapping("admin/product/baseCategoryTrademark")
public class BaseCategoryTrademarkController {


    @Autowired
    private BaseCategoryTrademarkService baseCategoryTrademarkService;


    /**
     * 根据category3Id获取可选品牌列表
     * GET/admin/product/baseCategoryTrademark/findCurrentTrademarkList/{category3Id}
     *
     * @param category3Id
     * @return
     */
    @ApiOperation("根据category3Id获取可选品牌列表")
    @GetMapping("findCurrentTrademarkList/{category3Id}")
    public Result<List<BaseTrademark>> findCurrentTrademarkList(@PathVariable Long category3Id) {
        List<BaseTrademark> baseTrademarkList = baseCategoryTrademarkService.findCurrentTrademarkList(category3Id);
        return Result.ok(baseTrademarkList);
    }

    @ApiOperation("根据category3Id获取品牌列表")
    @GetMapping("findTrademarkList/{category3Id}")
    public Result findTrademarkList(@PathVariable Long category3Id) {
        List<BaseTrademark> baseTrademarkList = baseCategoryTrademarkService.findTrademarkList(category3Id);
        return Result.ok(baseTrademarkList);
    }


    /**
     * 删除分类品牌关联
     * DELETE/admin/product/baseCategoryTrademark/remove/{category3Id}/{trademarkId}
     * @param category3Id
     * @param trademarkId
     * @return
     */
    @ApiOperation("删除分类品牌关联")
    @DeleteMapping("remove/{category3Id}/{trademarkId}")
    public Result remove(@PathVariable Long category3Id, @PathVariable Long trademarkId) {
        baseCategoryTrademarkService.remove(category3Id,trademarkId);

        return Result.ok();
    }

    /**
     * 保存分类品牌关联
     * POST/admin/product/baseCategoryTrademark/save
     * @param categoryTrademarkVo
     * @return
     */
    @PostMapping("save")
    public Result save(@RequestBody CategoryTrademarkVo categoryTrademarkVo){
        baseCategoryTrademarkService.save(categoryTrademarkVo);
        return Result.ok();
    }




}
