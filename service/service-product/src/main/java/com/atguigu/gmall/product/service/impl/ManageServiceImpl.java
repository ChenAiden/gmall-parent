package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuPosterMapper spuPosterMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitService rabbitService;


    /**
     * ??????????????????
     *
     * @return
     */
    @Override
    public List<BaseCategory1> getCategory1() {

        return baseCategory1Mapper.selectList(null);
    }

    /**
     * ??????????????????
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
     * ??????????????????
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
     * ????????????Id ????????????????????????
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
     * ??????-??????????????????    admin/product/saveAttrInfo
     * <p>
     * ??????????????????????????????????????????
     * <p>
     * ??????????????????????????????????????????????????????id???
     *
     * @param baseAttrInfo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)  //??????????????????
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        //??????id  ???????????????????????????
        if (baseAttrInfo.getId() != null) {
            //??????
            baseAttrInfoMapper.updateById(baseAttrInfo);

            //?????????????????????
            QueryWrapper<BaseAttrValue> wrapper = new QueryWrapper<>();
            wrapper.eq("attr_id", baseAttrInfo.getId());
            baseAttrValueMapper.delete(wrapper);

        } else {
            //??????
            baseAttrInfoMapper.insert(baseAttrInfo);
        }

        //??????????????????????????????BaseAttrValue
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        //??????????????????
        if (!CollectionUtils.isEmpty(attrValueList)) {

            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }
    }


    /**
     * ??????????????????Id ??????????????????????????????     /admin/product/getAttrValueList/{attrId}
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
     * spu????????????
     * admin/product/{page}/{limit}
     *
     * @param pageParam
     * @param spuInfo
     * @return
     */
    @Override
    public IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> pageParam, SpuInfo spuInfo) {
        QueryWrapper<SpuInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id", spuInfo.getCategory3Id());
        wrapper.orderByDesc("id");

        //????????????????????????      //????????????????????????
        Page<SpuInfo> spuInfoPage = spuInfoMapper.selectPage(pageParam, wrapper);
        return spuInfoPage;
    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrMapper.selectList(null);
        return baseSaleAttrList;
    }

    @Transactional(rollbackFor = Exception.class)//????????????
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //??????spuInfo???
        spuInfoMapper.insert(spuInfo);

        Long spuId = spuInfo.getId();

        //??????spu??????
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        //  ???????????????
        if (!CollectionUtils.isEmpty(spuImageList)) {
            //  ????????????
            for (SpuImage spuImage : spuImageList) {
                //  ?????????spuId ??????
                spuImage.setSpuId(spuId);
                //  ??????spuImge
                spuImageMapper.insert(spuImage);
            }
        }

        //??????spu??????
        List<SpuPoster> spuPosterList = spuInfo.getSpuPosterList();
        //  ???????????????
        if (!CollectionUtils.isEmpty(spuPosterList)) {
            //  ????????????
            for (SpuPoster spuPoster : spuPosterList) {
                //  ?????????spuId ??????
                spuPoster.setSpuId(spuId);
                //  ??????spuImge
                spuPosterMapper.insert(spuPoster);
            }
        }

        //??????spu???????????????
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (!CollectionUtils.isEmpty(spuSaleAttrList)) {
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuId);
                spuSaleAttrMapper.insert(spuSaleAttr);

                //??????spu??????????????????
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (!CollectionUtils.isEmpty(spuSaleAttrValueList)) {
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setSpuId(spuId);
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }
                }
            }
        }

    }

    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        QueryWrapper<SpuImage> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id", spuId);
        return spuImageMapper.selectList(wrapper);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {

        //?????????????????????
        RBloomFilter<Object> rbloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        //??????????????????????????????
        rbloomFilter.add(skuInfo.getId());

        skuInfoMapper.insert(skuInfo);

        Long skuId = skuInfo.getId();

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();

        if (!CollectionUtils.isEmpty(skuImageList)) {
            skuImageList.forEach(skuImage -> {
                skuImage.setSkuId(skuId);
                skuImageMapper.insert(skuImage);
            });
        }

        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            skuAttrValueList.forEach(skuAttrValue -> {
                skuAttrValue.setSkuId(skuId);
                skuAttrValueMapper.insert(skuAttrValue);
            });
        }

        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
                skuSaleAttrValue.setSkuId(skuId);
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            });
        }

    }

    @Override
    public IPage<SkuInfo> getSkuInfoPage(Page<SkuInfo> skuInfoPage, SkuInfo skuInfo) {
        QueryWrapper<SkuInfo> wrapper = new QueryWrapper<>();
        if (skuInfo.getCategory3Id() != null) {
            wrapper.eq("category3_id", skuInfo.getCategory3Id());
        }
        wrapper.orderByDesc("id");

        IPage<SkuInfo> skuInfoIPage = skuInfoMapper.selectPage(skuInfoPage, wrapper);
        return skuInfoIPage;
    }

    /**
     * ??????
     * @param skuId
     */
    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);

        //rabbitMq  ??????????????????
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_GOODS,MqConst.ROUTING_GOODS_LOWER,skuId);
    }

    /**
     * ??????
     * @param skuId
     */
    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);

        //rabbitMq  ??????????????????
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_GOODS,MqConst.ROUTING_GOODS_UPPER,skuId);
    }

    @GmallCache(prefix = "sku:", suffix = ":info")
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        return getSkuInfoDB(skuId);

//        return getSkuInfoRedis(skuId);

//        return getSkuInfoRedisson(skuId);
    }



    /*
    ???????????????????????????skuinfo???????????????

    1.??????key??????redis???????????????
        ???????????????????????????
     ???????????????
        2.???????????????
        ?????????????????????
        ???????????????????????????

        3.???????????????
        ??????????????????redis??????
        ??????????????????null???redis  ??????null

        4.?????????

     2.????????????mysql

     */
    private SkuInfo getSkuInfoRedis(Long skuId) {
        //?????????redis
        try {
            //??????key   sku + skuId + info
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;

            SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);

            if (skuInfo == null) {
                //???redis????????????????????? sku:lock
                String lockKey = RedisConst.SKUKEY_PREFIX + RedisConst.SKULOCK_SUFFIX;
                //????????????
                String uuid = UUID.randomUUID().toString().replace("-", "");
                //???????????????
                Boolean res = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);

                if (res) {
                    //???????????????
                    skuInfo = getSkuInfoDB(skuId);
                    try {
                        //????????????????????????mysql?????????
                        if (skuInfo == null) {
                            //mysql??????????????????
                            skuInfo = new SkuInfo();

                            redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);

                            return skuInfo;
                        } else {
                            //?????????redis???
                            redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);

                            return skuInfo;
                        }
                    } finally {
                        //?????????
                        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

                        DefaultRedisScript redisScript = new DefaultRedisScript();

                        redisScript.setResultType(Long.class);
                        redisScript.setScriptText(script);

                        redisTemplate.execute(redisScript, Arrays.asList(lockKey), uuid);
                    }
                } else {
                    //?????????????????????????????????
                    try {
                        Thread.sleep(10);
                        return getSkuInfoRedis(skuId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                return skuInfo;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //??????redis???????????????????????????????????????
        return getSkuInfoDB(skuId);
    }


    @Autowired
    private RedissonClient redissonClient;

    private SkuInfo getSkuInfoRedisson(Long skuId) {
        try {
            //????????????key
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            //???redis?????????
            SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);

            if (skuInfo == null) {

                //???????????????mysql?????????
                String lockKey = RedisConst.SKUKEY_PREFIX + RedisConst.SKUKEY_SUFFIX;

                RLock lock = redissonClient.getLock(lockKey);

                boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);

                if (res) {
                    //???????????????
                    skuInfo = getSkuInfoDB(skuId);

                    try {
                        if (skuInfo == null) {
                            skuInfo = new SkuInfo();

                            redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                            return skuInfo;
                        } else {
                            redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                            return skuInfo;
                        }
                    } finally {
                        lock.unlock();
                    }
                } else {
                    try {
                        Thread.sleep(10);
                        return getSkuInfoRedisson(skuId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                return skuInfo;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return getSkuInfoDB(skuId);

    }


    //?????????????????????skuinfo???????????????
    private SkuInfo getSkuInfoDB(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);

//        if (skuInfo.getIsSale() == 0) {
//            throw new GmallException("?????????????????????", 201);
//        }

        QueryWrapper<SkuImage> skuImageWrapper = new QueryWrapper<>();
        skuImageWrapper.eq("sku_id", skuId);
        List<SkuImage> skuImageList = skuImageMapper.selectList(skuImageWrapper);

        if (skuInfo != null) {
            skuInfo.setSkuImageList(skuImageList);
        }
        return skuInfo;
    }


    /**
     * ??????????????????
     * ??????????????????????????????
     *
     * @param skuId
     * @return
     */
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        //?????????
        String lockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_PREFIX;

        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();

        try {
            SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
            if (skuInfo != null) {
                return skuInfo.getPrice();
            }
        } finally {
            lock.unlock();
        }
        return new BigDecimal("0");
    }


    /**
     * GET/api/product/inner/findSpuPosterBySpuId/{spuId}  ??????spuId ??????????????????
     *
     * @param spuId
     * @return
     */
    @GmallCache(prefix = "SpuPosterBySpuId:")
    @Override
    public List<SpuPoster> findSpuPosterBySpuId(Long spuId) {
        QueryWrapper<SpuPoster> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id", spuId);

        return spuPosterMapper.selectList(wrapper);
    }


    /**
     * GET/api/product/inner/getCategoryView/{category3Id} ??????????????????id??????????????????
     *
     * @param category3Id
     * @return
     */
    @GmallCache(prefix = "CategoryView:", suffix = ":info")
    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {

        return baseCategoryViewMapper.selectById(category3Id);
    }

    @GmallCache(prefix = "SpuSaleAttrListCheckBySku:")
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        //??????????????????????????????24????????????????????????
        QueryWrapper<SkuSaleAttrValue> wrapper = new QueryWrapper<>();
        wrapper.eq("sku_id", skuId);
        wrapper.eq("spu_id", spuId);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            return new ArrayList<>();
        }

        return spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    @GmallCache(prefix = "SkuValueIdsMap:")
    @Override
    public Map getSkuValueIdsMap(Long spuId) {
        Map<Object, Object> map = new HashMap<>();
        // key = 125|123 ,value = 37
        List<Map> mapList = skuSaleAttrValueMapper.skuSaleAttrValueMapper(spuId);
        if (!CollectionUtils.isEmpty(mapList)) {
            for (Map skuMap : mapList) {
                map.put(skuMap.get("value_ids"), skuMap.get("sku_id"));
            }
        }
        return map;
    }

    /**
     * GET/api/product/inner/getAttrList/{skuId} ??????skuId ????????????????????????
     *
     * @param skuId
     * @return
     */
    @GmallCache(prefix = "AttrList:")
    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {

        return baseAttrInfoMapper.getAttrList(skuId);
    }

    @GmallCache(prefix = "BaseCategoryList:",suffix = ":info")
    @Override
    public List<JSONObject> getBaseCategoryList() {
        //???????????????????????????????????????
        List<JSONObject> list = new ArrayList<>();

        //????????????????????????
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);

        Map<Long, List<BaseCategoryView>> category1Map = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        int index = 0;
        for (Map.Entry<Long, List<BaseCategoryView>> category1Entry : category1Map.entrySet()) {
            //??????????????????????????????
            JSONObject jsonObject1 = new JSONObject();

            //?????????????????????id
            Long category1Id = category1Entry.getKey();
            //??????????????????????????????????????????
            List<BaseCategoryView> category1List = category1Entry.getValue();

            //?????????????????????????????????????????????????????????????????????????????????Category1Name???????????????
            String category1Name = category1List.get(0).getCategory1Name();

            jsonObject1.put("index", ++index);
            jsonObject1.put("categoryId", category1Id);
            jsonObject1.put("categoryName", category1Name);

            //????????????????????????????????????
            Map<Long, List<BaseCategoryView>> category2Map = category1List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));

            List<JSONObject> category2Child = new ArrayList<>();
            for (Map.Entry<Long, List<BaseCategoryView>> category2Entry : category2Map.entrySet()) {
                //??????????????????????????????
                JSONObject jsonObject2 = new JSONObject();

                //??????????????????id
                Long category2Id = category2Entry.getKey();
                //???????????????????????????????????????
                List<BaseCategoryView> category2List = category2Entry.getValue();
                //????????????????????????
                String category2Name = category2List.get(0).getCategory2Name();
                jsonObject2.put("categoryId", category2Id);
                jsonObject2.put("categoryName", category2Name);

                List<JSONObject> category3Child = new ArrayList<>();

                for (BaseCategoryView baseCategoryView : category2List) {
                    //??????????????????????????????
                    JSONObject jsonObject3 = new JSONObject();
                    jsonObject3.put("categoryId", baseCategoryView.getCategory3Id());
                    jsonObject3.put("categoryName", baseCategoryView.getCategory3Name());

                    category3Child.add(jsonObject3);
                }

                jsonObject2.put("categoryChild",category3Child);
                category2Child.add(jsonObject2);
            }

            jsonObject1.put("categoryChild",category2Child);
            list.add(jsonObject1);
        }

        return list;
    }


    /**
     * ??????????????????
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
