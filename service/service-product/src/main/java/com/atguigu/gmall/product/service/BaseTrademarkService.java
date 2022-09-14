package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Aiden
 * @create 2022-09-12 20:47
 */
public interface BaseTrademarkService extends IService<BaseTrademark> {

    IPage<BaseTrademark> getBaseTrademarkPage(Page<BaseTrademark> pageParam);

}
