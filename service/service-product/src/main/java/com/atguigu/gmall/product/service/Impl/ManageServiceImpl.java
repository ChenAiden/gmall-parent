package com.atguigu.gmall.product.service.Impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author Aiden
 * @create 2022-09-08 15:31
 */
@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    /**
     * 查询二级分类
     *
     * @return
     */
    @Override
    public List<BaseCategory1> getCategory1() {

        return baseCategory1Mapper.selectList(null);
    }

    /**
     * 查询二级分类
     *
     * @param category1Id
     * @return
     */
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        QueryWrapper<BaseCategory2> wrapper = new QueryWrapper<>();
        wrapper.eq("category1_id", category1Id);

        return baseCategory2Mapper.selectList(wrapper);

    }

    /**
     * 查询三级分类
     *
     * @param category2Id
     * @return
     */
    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        QueryWrapper<BaseCategory3> wrapper = new QueryWrapper<>();
        wrapper.eq("category2_id", category2Id);

        return baseCategory3Mapper.selectList(wrapper);
    }

    /**
     * 根据分类Id 获取平台属性集合
     *
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {

        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectAttrInfoList(category1Id, category2Id, category3Id);
        return baseAttrInfoList;
    }

    /**
     * 保存-修改平台属性    admin/product/saveAttrInfo
     *
     * 保存和修改使用的是同一个接口
     *
     * 先在主表中添加数据（副表需要添加主表id）
     *
     * @param baseAttrInfo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)  //添加事务注解
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        //通过id  判断是添加还是修改
        if (baseAttrInfo.getId() != null) {
            //修改
            baseAttrInfoMapper.updateById(baseAttrInfo);

            //删除副表旧数据
            QueryWrapper<BaseAttrValue> wrapper = new QueryWrapper<>();
            wrapper.eq("attr_id", baseAttrInfo.getId());
            baseAttrValueMapper.delete(wrapper);

        } else {
            //添加
            baseAttrInfoMapper.insert(baseAttrInfo);
        }

        //修改或添加都需要处理BaseAttrValue
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        //判断为不为空
        if (!CollectionUtils.isEmpty(attrValueList)){

            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }
    }


    /**
     * 根据平台属性Id 获取到平台属性值集合     /admin/product/getAttrValueList/{attrId}
     *
     * @param attrId
     * @return
     */
    @Override
    public BaseAttrInfo getAttrInfo(Long attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);

        List<BaseAttrValue> baseAttrValueList = getBaseAttrValueList(attrId);

        baseAttrInfo.setAttrValueList(baseAttrValueList);

        return baseAttrInfo;
    }


    /**
     * 获取属性集合
     *
     * @param attrId
     * @return
     */
    private List<BaseAttrValue> getBaseAttrValueList(Long attrId) {
        QueryWrapper<BaseAttrValue> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_id", attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.selectList(wrapper);
        return baseAttrValueList;
    }


}
