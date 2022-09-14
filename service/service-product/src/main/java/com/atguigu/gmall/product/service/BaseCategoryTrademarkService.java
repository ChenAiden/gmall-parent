package com.atguigu.gmall.product.service;


import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 三级分类表 服务类
 * </p>
 *
 * @author Aiden
 * @since 2022-09-12
 */
public interface BaseCategoryTrademarkService extends IService<BaseCategoryTrademark> {

    List<BaseTrademark> findCurrentTrademarkList(Long category3Id);

    List<BaseTrademark> findTrademarkList(Long category3Id);

    void remove(Long category3Id, Long trademarkId);

    void save(CategoryTrademarkVo categoryTrademarkVo);

}
