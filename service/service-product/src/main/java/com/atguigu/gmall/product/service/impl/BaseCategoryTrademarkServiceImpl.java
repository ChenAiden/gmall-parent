package com.atguigu.gmall.product.service.impl;


import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.atguigu.gmall.product.mapper.BaseCategoryTrademarkMapper;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 三级分类表 服务实现类
 * </p>
 *
 * @author Aiden
 * @since 2022-09-12
 */
@Service
public class BaseCategoryTrademarkServiceImpl extends ServiceImpl<BaseCategoryTrademarkMapper, BaseCategoryTrademark> implements BaseCategoryTrademarkService {

    @Autowired
    private BaseCategoryTrademarkMapper baseCategoryTrademarkMapper;

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;


    /**
     * 流操作：
     * stream（）.map（）
     * stream（）.filter（）
     *
     * 总结：
     *      选择上：如果处理之后的返回值结果和原来类型一样，数量变少则选择filter
     *             如果处理之后的返回值结果和原来类型不一样，数量不变则选择map
     *
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseTrademark> findCurrentTrademarkList(Long category3Id) {
        //  哪些是关联的品牌Id
        QueryWrapper<BaseCategoryTrademark> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id",category3Id);
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseCategoryTrademarkMapper.selectList(wrapper);

        //  判断
        if (!CollectionUtils.isEmpty(baseCategoryTrademarkList)){

            //map转化对象，将BaseCategoryTrademark转化为Long类型的TrademarkId
            List<Long> tradeMarkIdList = baseCategoryTrademarkList.stream()
                    .map(BaseCategoryTrademark::getTrademarkId).collect(Collectors.toList());   //map转化对象，将BaseCategoryTrademark转化为Long类型的TrademarkId

            //  在所有的品牌Id 中将这些有关联的品牌Id 给过滤掉就可以！
            //找到了拦截，!contains就是没有找到的   这里表示只要不在这个集合中的数
            List<BaseTrademark> baseTrademarkList = baseTrademarkMapper.selectList(null).stream()
                    .filter(baseTrademark -> !tradeMarkIdList.contains(baseTrademark.getId())).collect(Collectors.toList());

            //详细步骤
//            baseTrademarkMapper.selectList(null).stream().filter(new Predicate<BaseTrademark>() {
//                @Override
//                public boolean test(BaseTrademark baseTrademark) {
//                    boolean contains = tradeMarkIdList.contains(baseTrademark.getId());
//                    return !contains;//找到了拦截，!contains就是没有找到的   这里表示只要不在这个集合中的数
//                }
//            }).collect(Collectors.toList());

            //  返回数据
            return baseTrademarkList;
        }
        //  如果说这个三级分类Id 下 没有任何品牌！ 则获取到所有的品牌数据！
        return baseTrademarkMapper.selectList(null);
    }

    @Override
    public List<BaseTrademark> findTrademarkList(Long category3Id) {
        QueryWrapper<BaseCategoryTrademark> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id",category3Id);
        List<BaseCategoryTrademark> baseCategoryTrademarks = baseCategoryTrademarkMapper.selectList(wrapper);

        List<Long> tradeMarkIdList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(baseCategoryTrademarks)){

            tradeMarkIdList  = baseCategoryTrademarks.stream()
                    .map(BaseCategoryTrademark::getTrademarkId).collect(Collectors.toList());

//            List<BaseTrademark> list = new ArrayList<>();
//            for (BaseCategoryTrademark baseCategoryTrademark : baseCategoryTrademarks) {
//                Long trademarkId = baseCategoryTrademark.getTrademarkId();
//                BaseTrademark baseTrademark = baseTrademarkMapper.selectById(trademarkId);
//                list.add(baseTrademark);
//            }
//            return list;
        }
        return baseTrademarkMapper.selectBatchIds(tradeMarkIdList);
    }

    @Override
    public void remove(Long category3Id, Long trademarkId) {
        QueryWrapper<BaseCategoryTrademark> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id",category3Id);
        wrapper.eq("trademark_id",trademarkId);
        baseCategoryTrademarkMapper.delete(wrapper);
    }

    /**
     * 保存分类品牌关联
     *
     * 两种方式
     * this.saveBatch(baseCategoryTrademarkList)
     * baseCategoryTrademarkMapper.insert(baseCategoryTrademark)
     *
     * @param categoryTrademarkVo
     */
    @Override
    public void save(CategoryTrademarkVo categoryTrademarkVo) {
        Long category3Id = categoryTrademarkVo.getCategory3Id();
        List<Long> trademarkIdList = categoryTrademarkVo.getTrademarkIdList();

        if (!CollectionUtils.isEmpty(trademarkIdList)){

            List<BaseCategoryTrademark> baseCategoryTrademarkList = trademarkIdList.stream().map(trademarkId -> {
                BaseCategoryTrademark baseCategoryTrademark = new BaseCategoryTrademark();
                baseCategoryTrademark.setTrademarkId(trademarkId);
                baseCategoryTrademark.setCategory3Id(category3Id);

                return baseCategoryTrademark;
            }).collect(Collectors.toList());

            //  批量保存到数据库    baseCategoryTrademarkList
            this.saveBatch(baseCategoryTrademarkList);

//            for (Long trademarkId : trademarkIdList) {
//                BaseCategoryTrademark baseCategoryTrademark = new BaseCategoryTrademark();
//                baseCategoryTrademark.setTrademarkId(trademarkId);
//                baseCategoryTrademark.setCategory3Id(category3Id);
//
//                baseCategoryTrademarkMapper.insert(baseCategoryTrademark);
//            }
        }


    }
}
