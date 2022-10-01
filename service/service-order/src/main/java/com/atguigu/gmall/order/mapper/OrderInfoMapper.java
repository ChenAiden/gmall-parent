package com.atguigu.gmall.order.mapper;

import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Aiden
 * @create 2022-09-29 11:34
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    IPage<OrderInfo> selectOrderByPage(@Param("page") Page<OrderInfo> page, @Param("userId") String userId);

}
