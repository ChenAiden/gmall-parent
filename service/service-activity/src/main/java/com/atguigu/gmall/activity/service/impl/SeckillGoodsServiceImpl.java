package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Aiden
 * @create 2022-10-06 16:25
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Override
    public List<SeckillGoods> findAll() {
        List<SeckillGoods> seckillGoodsList = redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).values();

        return seckillGoodsList;
    }

    /**
     * 查询具体信息
     *
     * @param skuId
     * @return
     */
    @Override
    public SeckillGoods getSeckillGoods(String skuId) {
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId);
        return seckillGoods;
    }

    /**
     * 秒杀下单
     *
     * @param userId
     * @param skuId
     */
    @Override
    public void seckillUser(String userId, Long skuId) {
        //校验状态位
        String state = (String) CacheHelper.get(skuId.toString());

        if (!"1".equals(state)) {
            //状态不为1，则说明商品售罄
            return;
        }

        //校验是否下单
        Boolean result = redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_USER + userId, skuId,30, TimeUnit.MINUTES);
        if (!result) {
            //买过则不能再买
            return;
        }

        //校验库存
        String stockSkuId = (String) redisTemplate.opsForList().rightPop(RedisConst.SECKILL_STOCK_PREFIX + skuId);

        if (StringUtils.isEmpty(stockSkuId)) {
            //为空,说明没有了
            //更新状态位置
            redisTemplate.convertAndSend("seckillPush", skuId + ":0");

            return;
        }

        //生成临时订单
        OrderRecode orderRecode = new OrderRecode();
        orderRecode.setUserId(userId);
        orderRecode.setNum(1);
        orderRecode.setOrderStr(MD5.encrypt(userId + skuId));
        orderRecode.setSeckillGoods(this.getSeckillGoods(skuId.toString()));

        //存入redis做后续处理
        //key seckill:orders  value userId  orderRecode
        //                          userId  orderRecode
        redisTemplate.opsForHash().put(RedisConst.SECKILL_ORDERS, userId, orderRecode);


        //更新库存
        this.updateStockCount(skuId);
    }

    /**
     * 校验当前用户在秒杀中的状态
     *
     * @param userId
     * @param skuId
     * @return
     */
    @Override
    public Result checkOrder(String userId, Long skuId) {

        //判断用户是否获得秒杀资格并建立了临时订单
        Boolean result = redisTemplate.hasKey(RedisConst.SECKILL_USER + userId);

        if (result) {
            //通过是否存在临时订单，判断用户是否下单后支付了（支付后会删除临时订单）
            Boolean isExist = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).hasKey(userId);

            if (isExist) {
                //存在临时订单，返回215，让用户去支付
                OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);
                return Result.build(orderRecode, ResultCodeEnum.SECKILL_SUCCESS);
            }
        }

        //库存中不存在临时订单，判断是否是支付（支付后会有支付记录）
        Boolean isPay = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).hasKey(userId);

        if (isPay){
            return Result.build(null, ResultCodeEnum.SECKILL_ORDER_SUCCESS);
        }

        //不存在支付后信息  判断用户为什么没有操作，可能是无库存，可能是在派对还未处理到
        //是否库存？  校验状态码
        String state = (String) CacheHelper.get(skuId.toString());
        if ("0".equals(state)) {
            //状态为0，则说明商品售罄
            return Result.build(null, ResultCodeEnum.SECKILL_FINISH);
        }

        //排队中
        return Result.build(null, ResultCodeEnum.SECKILL_RUN);
    }

    /**
     * 更新库存
     *
     * @param skuId
     */
    private void updateStockCount(Long skuId) {
        Lock lock = new ReentrantLock();
        lock.lock();

        try {
            //获取当前库存
            Long size = redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + skuId).size();


            //可以5次取一次
            if (size % 2 == 0) {
                //更新redis,先更新那个都可以
                SeckillGoods seckillGoods = this.getSeckillGoods(skuId.toString());
                seckillGoods.setNum(size.intValue());//更新数量

                redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).put(skuId.toString(), seckillGoods);

                //更新mysql
                SeckillGoods mySqlSeckillGoods = new SeckillGoods();
                QueryWrapper<SeckillGoods> wrapper = new QueryWrapper<>();
                wrapper.eq("sku_id",seckillGoods.getSkuId());

                mySqlSeckillGoods.setNum(seckillGoods.getStockCount() - size.intValue());
                mySqlSeckillGoods.setStockCount(size.intValue());

                seckillGoodsMapper.update(mySqlSeckillGoods,wrapper);
            }

        } finally {
            lock.unlock();
        }
    }


}
