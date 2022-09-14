package com.atguigu.gmall.activity.service.impl;


import com.atguigu.gmall.activity.mapper.ActivityRuleMapper;
import com.atguigu.gmall.activity.mapper.ActivitySkuMapper;
import com.atguigu.gmall.activity.mapper.ActivityInfoMapper;
import com.atguigu.gmall.activity.service.ActivityInfoService;
import com.atguigu.gmall.activity.service.ActivityRuleService;
import com.atguigu.gmall.activity.service.ActivitySkuService;
import com.atguigu.gmall.model.activity.ActivityInfo;
import com.atguigu.gmall.model.activity.ActivityRule;
import com.atguigu.gmall.model.activity.ActivityRuleVo;
import com.atguigu.gmall.model.activity.ActivitySku;
import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 活动表 服务实现类
 * </p>
 *
 * @author Aiden
 * @since 2022-09-13
 */
@Service
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo> implements ActivityInfoService {

    @Autowired
    private ActivityInfoMapper activityInfoMapper;

    @Autowired
    private ActivityRuleMapper activityRuleMapper;

    @Autowired
    private ActivitySkuMapper activitySkuMapper;

    @Autowired
    private ActivityRuleService activityRuleService;

    @Autowired
    private ActivitySkuService activitySkuService;



//    @Autowired
//    private SkuInfoMapper skuInfoMapper;

    @Override
    public IPage<ActivityInfo> getActivityInfoPage(Page<ActivityInfo> activityInfoPage) {
        QueryWrapper<ActivityInfo> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("start_time");
        IPage<ActivityInfo> activityInfoIPage = activityInfoMapper.selectPage(activityInfoPage, wrapper);

        activityInfoIPage.setRecords(activityInfoIPage.getRecords().stream().map(activityInfo -> {
            if (activityInfo.getActivityType().equals("FULL_REDUCTION")) {
                activityInfo.setActivityTypeString("满减");
            } else if (activityInfo.getActivityType().equals("FULL_DISCOUNT")) {
                activityInfo.setActivityTypeString("折扣");
            }
            return activityInfo;
        }).collect(Collectors.toList()));

        return activityInfoIPage;
    }


    @Override
    public List<ActivityRule> findActivityRuleList(Long id) {
        QueryWrapper<ActivityRule> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", id);
        List<ActivityRule> activityRuleList = activityRuleMapper.selectList(wrapper);

        return activityRuleList;
    }

    @Override
    public List<SkuInfo> findSkuInfoList(Long id) {
        QueryWrapper<ActivitySku> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", id);
        List<ActivitySku> activitySkuList = activitySkuMapper.selectList(wrapper);

        List<SkuInfo> skuInfoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(activitySkuList)){
//            skuInfoList = activitySkuList.stream()
//                    .map(activitySku -> skuInfoMapper.selectById(activitySku.getId())).collect(Collectors.toList());
        }
        return skuInfoList;
    }

    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {

        //根据sku_name模糊查询
        return null;
    }

    @Override
    public void saveActivityRule(ActivityRuleVo activityRuleVo) {

        List<ActivityRule> activityRuleList = activityRuleVo.getActivityRuleList();
        List<ActivitySku> activitySkuList = activityRuleVo.getActivitySkuList();

        activityRuleService.saveBatch(activityRuleList);
        activitySkuService.saveBatch(activitySkuList);
    }

}
