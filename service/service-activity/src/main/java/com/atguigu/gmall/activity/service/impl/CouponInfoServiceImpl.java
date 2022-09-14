package com.atguigu.gmall.activity.service.impl;


import com.atguigu.gmall.activity.mapper.CouponInfoMapper;
import com.atguigu.gmall.activity.service.CouponInfoService;
import com.atguigu.gmall.model.activity.CouponInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 优惠券表 服务实现类
 * </p>
 *
 * @author Aiden
 * @since 2022-09-13
 */
@Service
public class CouponInfoServiceImpl extends ServiceImpl<CouponInfoMapper, CouponInfo> implements CouponInfoService {

    @Autowired
    private CouponInfoMapper couponInfoMapper;

    @Override
    public IPage<CouponInfo> getCouponInfoPage(Page<CouponInfo> couponInfoPage) {
        QueryWrapper<CouponInfo> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("start_time");
        IPage<CouponInfo> couponInfoIPage = couponInfoMapper.selectPage(couponInfoPage, wrapper);

        return couponInfoIPage;
    }

    @Override
    public List<CouponInfo> findCouponInfoList(Long id) {
        QueryWrapper<CouponInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id",id);
        List<CouponInfo> couponInfoList = couponInfoMapper.selectList(wrapper);
        return couponInfoList;
    }
}
