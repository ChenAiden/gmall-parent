package com.atguigu.gmall.activity.controller;


import com.atguigu.gmall.activity.service.ActivityInfoService;
import com.atguigu.gmall.activity.service.CouponInfoService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.ActivityInfo;
import com.atguigu.gmall.model.activity.ActivityRule;
import com.atguigu.gmall.model.activity.ActivityRuleVo;
import com.atguigu.gmall.model.activity.CouponInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 活动表 前端控制器
 * </p>
 *
 * @author Aiden
 * @since 2022-09-13
 */
@Api(tags = "促销活动")
@RestController
@RequestMapping("/admin/activity/activityInfo")
public class ActivityInfoController {

    @Autowired
    private ActivityInfoService activityInfoService;

    @Autowired
    private CouponInfoService couponInfoService;


    /**
     * GET/admin/activity/activityInfo/{page}/{limit} 分页
     * @param page
     * @param limit
     * @return
     */
    @ApiOperation("分页")
    @GetMapping("/{page}/{limit}")
    public Result<IPage<ActivityInfo>> getActivityInfoPage(@PathVariable Long page, @PathVariable Long limit) {

        Page<ActivityInfo> activityInfoPage = new Page<>(page, limit);
        IPage<ActivityInfo> activityInfoIPage = activityInfoService.getActivityInfoPage(activityInfoPage);
        return Result.ok(activityInfoIPage);
    }


    /**
     * fetchRuleDataById(id) {
     *       api.findActivityRuleList(id).then(response => {
     *
     *         this.activityRuleList = response.data.activityRuleList || []
     *         this.skuInfoList = response.data.skuInfoList || []
     *         this.couponInfoList = response.data.couponInfoList || []
     *
     *         this.listLoading = false
     *       })
     *     },
     */

    /**
     *  GET/admin/activity/activityInfo/findActivityRuleList/{id} 根据活动id获取活动规则
     * @param id
     * @return
     */
    @ApiOperation("根据活动id获取活动规则")
    @GetMapping("/findActivityRuleList/{id}")
    public Result<Map<String, Object>> findActivityRuleList(@PathVariable Long id){

        List<ActivityRule> activityRuleList = activityInfoService.findActivityRuleList(id);
        List<SkuInfo> skuInfoList = activityInfoService.findSkuInfoList(id);
        List<CouponInfo> couponInfoList = couponInfoService.findCouponInfoList(id);

        Map<String, Object> map = new HashMap<>();
        map.put("activityRuleList",activityRuleList);
        map.put("skuInfoList",skuInfoList);
        map.put("couponInfoList",couponInfoList);

        return Result.ok(map);
    }

    //GET/admin/activity/activityInfo/get/{id}
    @ApiOperation("根据id获取详情")
    @GetMapping("/get/{id}")
    public Result<ActivityInfo> get(@PathVariable Long id){

        ActivityInfo activityInfo = activityInfoService.getById(id);

        if (activityInfo.getActivityType().equals("FULL_REDUCTION")){
            activityInfo.setActivityTypeString("满减");
        }else if (activityInfo.getActivityType().equals("FULL_DISCOUNT")){
            activityInfo.setActivityTypeString("折扣");
        }
        return Result.ok(activityInfo);
    }

    //GET/admin/activity/activityInfo/findSkuInfoByKeyword/{keyword}
    @ApiOperation("根据关键字获取sku")
    @GetMapping("/findSkuInfoByKeyword/{keyword}")
    public Result<List<SkuInfo>> findSkuInfoByKeyword(@PathVariable String keyword){

        List<SkuInfo> skuInfoList = activityInfoService.findSkuInfoByKeyword(keyword);
        return Result.ok(skuInfoList);
    }


    /**
     *  /admin/activity/activityInfo/saveActivityRule 保存活动规则
     * @param activityRuleVo
     * @return
     */
    @ApiOperation("保存活动规则")
    @PostMapping("/saveActivityRule")
    public Result saveActivityRule(@RequestBody ActivityRuleVo activityRuleVo){

        activityInfoService.saveActivityRule(activityRuleVo);
        return Result.ok();
    }


    /**
     * http://localhost/admin/activity/activityInfo/save  添加活动信息
     * @param activityInfo
     * @return
     */
    @ApiOperation("添加活动信息")
    @PostMapping("/save")
    public Result save(@RequestBody ActivityInfo activityInfo){

        activityInfoService.save(activityInfo);
        return Result.ok();
    }


    /**
     * admin/activity/activityInfo/update  修改
     * @param activityInfo
     * @return
     */
    @ApiOperation("修改")
    @PutMapping("/update")
    public Result update(@RequestBody ActivityInfo activityInfo){

        activityInfoService.updateById(activityInfo);
        return Result.ok();
    }


    /**
     * /admin/activity/activityInfo/remove/{id} 删除
     * @param id
     * @return
     */
    @ApiOperation("删除")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable Long id){

        activityInfoService.removeById(id);
        return Result.ok();
    }

    // DELETE/admin/activity/activityInfo/batchRemove
    @ApiOperation("批量删除")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<Long> ids){

        activityInfoService.removeByIds(ids);
        return Result.ok();
    }


}

