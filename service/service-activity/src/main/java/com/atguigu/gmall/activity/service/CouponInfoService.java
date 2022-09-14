package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.model.activity.CouponInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 优惠券表 服务类
 * </p>
 *
 * @author Aiden
 * @since 2022-09-13
 */
public interface CouponInfoService extends IService<CouponInfo> {

    IPage<CouponInfo> getCouponInfoPage(Page<CouponInfo> couponInfoPage);

    List<CouponInfo> findCouponInfoList(Long id);

}
