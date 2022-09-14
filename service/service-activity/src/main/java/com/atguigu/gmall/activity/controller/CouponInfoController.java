package com.atguigu.gmall.activity.controller;


import com.atguigu.gmall.activity.service.CouponInfoService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.CouponInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 优惠券表 前端控制器
 * </p>
 *
 * @author Aiden
 * @since 2022-09-13
 */
@Api("优惠劵")
@RestController
@RequestMapping("/admin/activity/couponInfo")
public class CouponInfoController {

    //GET/admin/activity/couponInfo/{page}/{limit}

    @Autowired
    private CouponInfoService couponInfoService;

    //GET/admin/activity/couponInfo/{page}/{limit}
    @ApiOperation("分页")
    @GetMapping("/{page}/{limit}")
    public Result<IPage<CouponInfo>> getCouponInfoPage(@PathVariable Long page, @PathVariable Long limit) {

        Page<CouponInfo> couponInfoPage = new Page<>(page, limit);
        IPage<CouponInfo> couponInfoIPage = couponInfoService.getCouponInfoPage(couponInfoPage);
        return Result.ok(couponInfoIPage);
    }

}

