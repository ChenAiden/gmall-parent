package com.atguigu.gmall.activity.receiver;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * @author Aiden
 * @create 2022-10-06 14:52
 */
@Component
public class SeckillReceiver {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsService seckillGoodsService;


    /**
     * 秒杀商品相关数据存入redis
     *
     * @param message
     * @param channel
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_1, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_1}
    ))
    public void SeckillGoodsToRedis(Message message, Channel channel) {

        System.out.println("我开始工作了，存入缓存...");

        try {
            QueryWrapper<SeckillGoods> wrapper = new QueryWrapper<>();
            //是否通过审核 0未审核 1通过 2已售罄
            wrapper.eq("status", "1");

            //当天的商品
            //select DATE_FORMAT(start_time,'%Y-%m-%d') from seckill_goods
            wrapper.eq("DATE_FORMAT(start_time,'%Y-%m-%d')", DateUtil.formatDate(new Date()));

            //是否有库存
            wrapper.gt("stock_count", 0);

            //获得当天秒杀商品列表
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(wrapper);

            if (!CollectionUtils.isEmpty(seckillGoodsList)) {

                for (SeckillGoods seckillGoods : seckillGoodsList) {
                    //判断当前商品是否已经存入
                    Boolean hasKey = redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).hasKey(seckillGoods.getSkuId().toString());

                    if (hasKey) {
                        //说明本次的商品信息已经存入过了
                        continue;
                    }
                    //存储基本数据seckillGoods
                    redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).put(seckillGoods.getSkuId().toString(), seckillGoods);

                    //有多少库存就存储多少次
                    for (int i = 0; i < seckillGoods.getStockCount(); i++) {
                        //存储skuId-redis-list防止超卖
                        redisTemplate.opsForList().leftPush(RedisConst.SECKILL_STOCK_PREFIX + seckillGoods.getSkuId(), seckillGoods.getSkuId().toString());
                    }

                    //发送状态位21
                    redisTemplate.convertAndSend("seckillPush", seckillGoods.getSkuId() + ":1");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


    /**
     * 监听  将用户和秒杀商品信息放入队列，异步处理
     *
     * @param userRecode
     * @param message
     * @param channel
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_SECKILL_USER, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_USER),
            key = {MqConst.ROUTING_SECKILL_USER}
    ))
    public void SeckillUser(UserRecode userRecode, Message message, Channel channel) {

        try {
            if (userRecode != null) {
                seckillGoodsService.seckillUser(userRecode.getUserId(),userRecode.getSkuId());
            }
        } catch (Exception e) {
            //出错就写入日志，人工处理
            e.printStackTrace();
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


    /**
     * 删除redis中的无用秒杀商品相关数据
     *
     * @param message
     * @param channel
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_18, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_18}
    ))
    public void deleteToRedis(Message message, Channel channel) {

        System.out.println("我开始工作了，删除无用缓存...");

        try {
            QueryWrapper<SeckillGoods> wrapper = new QueryWrapper<>();
            //是否通过审核 0未审核 1通过 2已售罄
            wrapper.eq("status", "1");
            //超时的秒杀订单
            wrapper.lt("end_time", new Date());

            //获得当天  超时的  秒杀商品列表
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(wrapper);

            if (!CollectionUtils.isEmpty(seckillGoodsList)){
                for (SeckillGoods seckillGoods : seckillGoodsList) {

                    Long skuId = seckillGoods.getSkuId();

                    //删除过期商品
                    redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).delete(skuId.toString());

                    //删除过期商品库存
                    redisTemplate.delete(RedisConst.SECKILL_STOCK_PREFIX + skuId);

                    //修改数据,遍历时修改
                    seckillGoods.setStatus("2");
    //                seckillGoodsMapper.update(seckillGoods,wrapper);
                }

                //用户是否下过单状态码有过期时间自动删除

                //删除临时订单
                redisTemplate.delete(RedisConst.SECKILL_ORDERS);
                //删除支付的订单
                redisTemplate.delete(RedisConst.SECKILL_ORDERS_USERS);


                //最后在修改,一次修改多个数据，性能好
                SeckillGoods seckillGoods = new SeckillGoods();
                seckillGoods.setStatus("2");

                seckillGoodsMapper.update(seckillGoods,wrapper);
            }
        } catch (Exception e) {
            //出错写入日志
            e.printStackTrace();
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
