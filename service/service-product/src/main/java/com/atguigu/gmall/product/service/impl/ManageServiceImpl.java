package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.RedisConst;
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
     * <p>
     * 保存和修改使用的是同一个接口
     * <p>
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
        if (!CollectionUtils.isEmpty(attrValueList)) {

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
     * spu分页列表
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

        //参数一：分页条件      //参数二：过滤条件
        Page<SpuInfo> spuInfoPage = spuInfoMapper.selectPage(pageParam, wrapper);
        return spuInfoPage;
    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrMapper.selectList(null);
        return baseSaleAttrList;
    }

    @Transactional(rollbackFor = Exception.class)//添加事务
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //添加spuInfo表
        spuInfoMapper.insert(spuInfo);

        Long spuId = spuInfo.getId();

        //添加spu图片
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        //  判断不为空
        if (!CollectionUtils.isEmpty(spuImageList)) {
            //  循环遍历
            for (SpuImage spuImage : spuImageList) {
                //  需要将spuId 赋值
                spuImage.setSpuId(spuId);
                //  保存spuImge
                spuImageMapper.insert(spuImage);
            }
        }

        //添加spu海报
        List<SpuPoster> spuPosterList = spuInfo.getSpuPosterList();
        //  判断不为空
        if (!CollectionUtils.isEmpty(spuPosterList)) {
            //  循环遍历
            for (SpuPoster spuPoster : spuPosterList) {
                //  需要将spuId 赋值
                spuPoster.setSpuId(spuId);
                //  保存spuImge
                spuPosterMapper.insert(spuPoster);
            }
        }

        //添加spu销售属性表
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (!CollectionUtils.isEmpty(spuSaleAttrList)) {
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuId);
                spuSaleAttrMapper.insert(spuSaleAttr);

                //添加spu销售属性值表
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

        //获取布隆过滤器
        RBloomFilter<Object> rbloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        //向布隆过滤器中添加值
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

    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);

        //TODO 后续还要发送消息队列更改数据库库存   更改ElasticSearch中的信息
    }

    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
    }

    @GmallCache(prefix = "sku:", suffix = ":info")
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        return getSkuInfoDB(skuId);

//        return getSkuInfoRedis(skuId);

//        return getSkuInfoRedisson(skuId);
    }



    /*
    从数据库中获取数据skuinfo和数据列表

    1.定义key，从redis中获取数据
        获取到了数据：返回
     没有数据：
        2.常数获取锁
        没有获得锁自旋
        得到锁，查询数据库

        3.查询数据库
        查到了存储到redis返回
        没有查询存储null到redis  返回null

        4.释放锁

     2.兜底查询mysql

     */
    private SkuInfo getSkuInfoRedis(Long skuId) {
        //尝试走redis
        try {
            //定义key   sku + skuId + info
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;

            SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);

            if (skuInfo == null) {
                //从redis中没有获取数据 sku:lock
                String lockKey = RedisConst.SKUKEY_PREFIX + RedisConst.SKULOCK_SUFFIX;
                //获取锁值
                String uuid = UUID.randomUUID().toString().replace("-", "");
                //尝试获取锁
                Boolean res = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);

                if (res) {
                    //获取到了锁
                    skuInfo = getSkuInfoDB(skuId);
                    try {
                        //判断是否获取到了mysql的数据
                        if (skuInfo == null) {
                            //mysql中也没有数据
                            skuInfo = new SkuInfo();

                            redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);

                            return skuInfo;
                        } else {
                            //存储到redis中
                            redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);

                            return skuInfo;
                        }
                    } finally {
                        //释放锁
                        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

                        DefaultRedisScript redisScript = new DefaultRedisScript();

                        redisScript.setResultType(Long.class);
                        redisScript.setScriptText(script);

                        redisTemplate.execute(redisScript, Arrays.asList(lockKey), uuid);
                    }
                } else {
                    //没有获得锁，则进行自旋
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

        //操作redis的过程中出现异常，兜底方法
        return getSkuInfoDB(skuId);
    }


    @Autowired
    private RedissonClient redissonClient;

    private SkuInfo getSkuInfoRedisson(Long skuId) {
        try {
            //定义数据key
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            //从redis中获取
            SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);

            if (skuInfo == null) {

                //获取锁，去mysql中查询
                String lockKey = RedisConst.SKUKEY_PREFIX + RedisConst.SKUKEY_SUFFIX;

                RLock lock = redissonClient.getLock(lockKey);

                boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);

                if (res) {
                    //查询数据库
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


    //从数据库中获取skuinfo和图片信息
    private SkuInfo getSkuInfoDB(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);

//        if (skuInfo.getIsSale() == 0) {
//            throw new GmallException("此商品还未上架", 201);
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
     * 获取商品价格
     * 加锁解决缓存穿透问题
     *
     * @param skuId
     * @return
     */
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        //定义锁
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
     * GET/api/product/inner/findSpuPosterBySpuId/{spuId}  根据spuId 获取海报数据
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
     * GET/api/product/inner/getCategoryView/{category3Id} 根据三级分类id获取分类信息
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
        //防止在缓存中存入一个24小时的不匹配数据
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
     * GET/api/product/inner/getAttrList/{skuId} 根据skuId 获取平台属性数据
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
        //创建外层集合，封装所有数据
        List<JSONObject> list = new ArrayList<>();

        //获取所有分类数据
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);

        Map<Long, List<BaseCategoryView>> category1Map = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        int index = 0;
        for (Map.Entry<Long, List<BaseCategoryView>> category1Entry : category1Map.entrySet()) {
            //创建一级分类封装对象
            JSONObject jsonObject1 = new JSONObject();

            //获取一级对象的id
            Long category1Id = category1Entry.getKey();
            //获取一级下对应的二级分类集合
            List<BaseCategoryView> category1List = category1Entry.getValue();

            //一级分类名字（集合中的每个元素都是一个完整的对象，都有Category1Name这个参数）
            String category1Name = category1List.get(0).getCategory1Name();

            jsonObject1.put("index", ++index);
            jsonObject1.put("categoryId", category1Id);
            jsonObject1.put("categoryName", category1Name);

            //对二级分类的集合实现分组
            Map<Long, List<BaseCategoryView>> category2Map = category1List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));

            List<JSONObject> category2Child = new ArrayList<>();
            for (Map.Entry<Long, List<BaseCategoryView>> category2Entry : category2Map.entrySet()) {
                //创建二级分类封装对象
                JSONObject jsonObject2 = new JSONObject();

                //获取二级分类id
                Long category2Id = category2Entry.getKey();
                //获取二级对应的三级分类集合
                List<BaseCategoryView> category2List = category2Entry.getValue();
                //获取二级分类名字
                String category2Name = category2List.get(0).getCategory2Name();
                jsonObject2.put("categoryId", category2Id);
                jsonObject2.put("categoryName", category2Name);

                List<JSONObject> category3Child = new ArrayList<>();

                for (BaseCategoryView baseCategoryView : category2List) {
                    //创建三级分类封装对象
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
