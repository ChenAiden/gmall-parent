package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.product.service.ManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Aiden
 * @create 2022-09-08 15:32
 */
@Api(tags = "商品管理接口")
@RestController
@RequestMapping("admin/product")
public class BaseManageController {

    @Autowired
    private ManageService manageService;

    /**
     * 查询三级分类
     * @param category2Id
     * @return
     */
    @GetMapping("/getCategory3/{category2Id}")
    public Result<List<BaseCategory3>> getCategory3(@PathVariable Long category2Id){

        List<BaseCategory3> baseCategory3List = manageService.getCategory3(category2Id);

        return Result.ok(baseCategory3List);
    }


    /**
     * 查询二级分类
     * @param category1Id
     * @return
     */
    @GetMapping("/getCategory2/{category1Id}")
    public Result<List<BaseCategory2>> getCategory2(@PathVariable Long category1Id){

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


