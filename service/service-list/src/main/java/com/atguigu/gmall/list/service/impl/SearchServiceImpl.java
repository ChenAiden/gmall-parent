package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.list.repositroy.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Aiden
 * @create 2022-09-23 10:13
 */
@Service
public class SearchServiceImpl implements SearchService {

    //使用repository可以启动项目自动创建索引库
    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProductFeignClient productFeignClient;


    @Override
    public void upperGoods(Long skuId) {
        //创建对象
        Goods goods = new Goods();

        //根据skuId查询skuInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        if (skuInfo != null) {

            goods.setId(skuInfo.getId());
            goods.setTitle(skuInfo.getSkuName());
            goods.setPrice(skuInfo.getPrice().doubleValue());
            goods.setCreateTime(new Date());
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());


            //根据品牌id查询品牌数据对象
            Long tmId = skuInfo.getTmId();
            BaseTrademark trademark = productFeignClient.getTrademark(tmId);
            if (trademark != null) {
                goods.setTmId(tmId);
                goods.setTmName(trademark.getTmName());
                goods.setTmLogoUrl(trademark.getLogoUrl());
            }

            //根据三级分类id查询三级分类数据
            Long category3Id = skuInfo.getCategory3Id();
            BaseCategoryView categoryView = productFeignClient.getCategoryView(category3Id);
            if (categoryView != null){
                goods.setCategory1Id(categoryView.getCategory1Id());
                goods.setCategory2Id(categoryView.getCategory2Id());
                goods.setCategory3Id(categoryView.getCategory3Id());
                goods.setCategory1Name(categoryView.getCategory1Name());
                goods.setCategory2Name(categoryView.getCategory2Name());
                goods.setCategory3Name(categoryView.getCategory3Name());
            }

            //根据平台属性id查询对应平台属性和属性值
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            if (!CollectionUtils.isEmpty(attrList)){

                List<SearchAttr> searchAttrList = attrList.stream().map(baseAttrInfo -> {
                    SearchAttr searchAttr = new SearchAttr();
                    searchAttr.setAttrId(baseAttrInfo.getId());
                    searchAttr.setAttrName(baseAttrInfo.getAttrName());
                    searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
                    return searchAttr;
                }).collect(Collectors.toList());

                goods.setAttrs(searchAttrList);
            }
        }
        //添加索引
        goodsRepository.save(goods);
    }

    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    /**
     * GET/api/list/inner/incrHotScore/{skuId}  更新商品的热度排名
     *
     * redis 的五种数据类型
     * string
     * hash
     * list
     * set
     * zset  hotScore shkId:21 评分
     *
     * @param skuId
     */
    @Override
    public void incrHotScore(Long skuId) {
        //引入redis自增
        String hotScore = "hotScore";
        Double score = redisTemplate.opsForZSet().incrementScore(hotScore,"skuId:" + skuId, 1);

        //每10次更新一次
        if (score % 10 == 0){
            Optional<Goods> optional = goodsRepository.findById(skuId);
            Goods goods = optional.get();
            goods.setId(skuId);
            goods.setHotScore(Math.round(score));
            goodsRepository.save(goods);
        }
    }




}
