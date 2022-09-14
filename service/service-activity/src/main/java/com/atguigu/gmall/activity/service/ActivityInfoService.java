package com.atguigu.gmall.activity.service;


import com.atguigu.gmall.model.activity.ActivityInfo;
import com.atguigu.gmall.model.activity.ActivityRule;
import com.atguigu.gmall.model.activity.ActivityRuleVo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 活动表 服务类
 * </p>
 *
 * @author Aiden
 * @since 2022-09-13
 */
public interface ActivityInfoService extends IService<ActivityInfo> {

    IPage<ActivityInfo> getActivityInfoPage(Page<ActivityInfo> activityInfoPage);

    List<ActivityRule> findActivityRuleList(Long id);

    List<SkuInfo> findSkuInfoList(Long id);

    List<SkuInfo> findSkuInfoByKeyword(String keyword);

    void saveActivityRule(ActivityRuleVo activityRuleVo);

}
