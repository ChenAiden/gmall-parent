package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Aiden
 * @create 2022-09-08 15:32
 */
//@CrossOrigin
@Api(tags = "商品管理接口")
@RestController
@RequestMapping("admin/product")
public class BaseManageController {

    @Autowired
    private ManageService manageService;

    /**
     * 根据平台属性Id 获取到平台属性值集合     /admin/product/getAttrValueList/{attrId}
     * @param attrId
     * @return
     */
    @ApiOperation("根据平台属性Id 获取到平台属性值集合")
    @GetMapping("/getAttrValueList/{attrId}")
    public Result<List<BaseAttrValue>> getAttrValueList(@PathVariable Long attrId){

        BaseAttrInfo baseAttrInfo = manageService.getAttrInfo(attrId);
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        return Result.ok(attrValueList);
    }


    /**
     * 保存-修改平台属性    admin/product/saveAttrInfo
     *
     * @param baseAttrInfo
     * @return
     */
    @ApiOperation("保存/修改")
    @PostMapping("/saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        manageService.saveAttrInfo(baseAttrInfo);

        return Result.ok();
    }


    /**
     * 根据分类Id 获取平台属性集合
     *
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @ApiOperation("根据分类Id 获取平台属性集合")
    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result<List<BaseAttrInfo>> attrInfoList(@PathVariable Long category1Id,
                                                   @PathVariable Long category2Id,
                                                   @PathVariable Long category3Id) {

        List<BaseAttrInfo> baseAttrInfoList = manageService.getAttrInfoList(category1Id, category2Id, category3Id);

        return Result.ok(baseAttrInfoList);
    }


    /**
     * 查询三级分类
     *
     * @param category2Id
     * @return
     */
    @GetMapping("/getCategory3/{category2Id}")
    public Result<List<BaseCategory3>> getCategory3(@PathVariable Long category2Id) {

        List<BaseCategory3> baseCategory3List = manageService.getCategory3(category2Id);

        return Result.ok(baseCategory3List);
    }


    /**
     * 查询二级分类
     *
     * @param category1Id
     * @return
     */
    @GetMapping("/getCategory2/{category1Id}")
    public Result<List<BaseCategory2>> getCategory2(@PathVariable Long category1Id) {

        List<BaseCategory2> baseCategory2List = manageService.getCategory2(category1Id);

        return Result.ok(baseCategory2List);
    }


    /**
     * 查询一级分类
     *
     * @return
     */
    @ApiOperation(value = "查询一级分类")
    @GetMapping("/getCategory1")
    public Result<List<BaseCategory1>> getCategory1() {

        List<BaseCategory1> baseCategory1List = manageService.getCategory1();

        return Result.ok(baseCategory1List);
    }

}


