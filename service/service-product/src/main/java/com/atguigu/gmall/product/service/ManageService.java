package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Aiden
 * @create 2022-09-08 15:25
 */
public interface ManageService {

    /**
     * 查询一级分类
     *
     * @return
     */
    List<BaseCategory1> getCategory1();

    /**
     * 查询二级分类
     *
     * @param category1Id
     * @return
     */
    List<BaseCategory2> getCategory2(Long category1Id);

    /**
     * 查询三级分类
     *
     * @param category2Id
     * @return
     */
    List<BaseCategory3> getCategory3(Long category2Id);

    /**
     * 根据分类Id 获取平台属性集合
     *
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);

    /**
     * 保存-修改平台属性    admin/product/saveAttrInfo
     *
     * @param baseAttrInfo
     * @return
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据平台属性Id 获取到平台属性值集合     /admin/product/getAttrValueList/{attrId}
     * @param attrId
     * @return
     */
    BaseAttrInfo getAttrInfo(Long attrId);

    /**
     * spu分页列表
     * admin/product/{page}/{limit}
     * @param pageParam
     * @param spuInfo
     * @return
     */
    IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> pageParam, SpuInfo spuInfo);

    List<BaseSaleAttr> baseSaleAttrList();

    void saveSpuInfo(SpuInfo spuInfo);

    List<SpuImage> getSpuImageList(Long spuId);

    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);

    void saveSkuInfo(SkuInfo skuInfo);

    IPage<SkuInfo> getSkuInfoPage(Page<SkuInfo> skuInfoPage, SkuInfo spuInfo);

    void cancelSale(Long skuId);

    void onSale(Long skuId);


    SkuInfo getSkuInfo(Long skuId);

    BigDecimal getSkuPrice(Long skuId);

    List<SpuPoster> findSpuPosterBySpuId(Long spuId);

    BaseCategoryView getCategoryView(Long category3Id);

    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);

    Map getSkuValueIdsMap(Long spuId);

    List<BaseAttrInfo> getAttrList(Long skuId);

    List<JSONObject> getBaseCategoryList();

}
