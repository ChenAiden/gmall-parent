package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author Aiden
 * @create 2022-10-06 16:23
 */
@RestController
@RequestMapping("/api/activity/seckill")
public class SeckillApiController {

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private OrderFeignClient orderFeignClient;

    /**
     * 获取所有秒杀产品
     *
     * @return
     */
    @GetMapping("/findAll")
    public Result<List<SeckillGoods>> findAll() {

        List<SeckillGoods> seckillGoodsList = seckillGoodsService.findAll();

        return Result.ok(seckillGoodsList);
    }


    /**
     * 查询具体商品
     * GET/api/activity/seckill/getSeckillGoods/{skuId}
     *
     * @param skuId
     * @return
     */
    @GetMapping("/getSeckillGoods/{skuId}")
    public Result getSeckillGoods(@PathVariable String skuId) {

        SeckillGoods seckillGoods = seckillGoodsService.getSeckillGoods(skuId);

        return Result.ok(seckillGoods);
    }


    /**
     * GET/api/activity/seckill/auth/getSeckillSkuIdStr/{skuId}  获取下单码
     *
     * @param skuId
     * @param request
     * @return
     */
    @GetMapping("/auth/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable Long skuId,
                                     HttpServletRequest request) {

        String userId = AuthContextHolder.getUserId(request);


        //获取商品信息，确认在获取的合法时间内
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId.toString());

        if (seckillGoods != null) {
            Date curTime = new Date();

            //校验当前时间是否是秒杀时间，防止用户提前获取下单码
            // beginDate  <= endDate     return true;
            if (DateUtil.dateCompare(seckillGoods.getStartTime(), curTime) && DateUtil.dateCompare(curTime, seckillGoods.getEndTime())) {

                //签发下单码
                String skuIdStr = MD5.encrypt(userId);
                return Result.ok(skuIdStr);
            }
        }
        return Result.fail().message("获取下单码失败");
    }


    /**
     * POST/api/activity/seckill/auth/seckillOrder/{skuId}  秒杀下单
     *
     * @param skuId
     * @param request
     * @return
     */
    @PostMapping("/auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable Long skuId,
                               HttpServletRequest request) {

        String userId = AuthContextHolder.getUserId(request);

        //校验下单码
        String skuIdStr = request.getParameter("skuIdStr");

        if (!MD5.encrypt(userId).equals(skuIdStr)) {
            return Result.build(null, ResultCodeEnum.ILLEGAL_REQUEST);
        }

        //校验状态位
        String state = (String) CacheHelper.get(skuId.toString());

        if (StringUtils.isEmpty(state)) {
            return Result.build(null, ResultCodeEnum.ILLEGAL_REQUEST);
        }

        if (!"1".equals(state)) {
            return Result.build(null, ResultCodeEnum.SECKILL_FINISH);
        } else {
            //构建对象  标识用户想购买那个商品
            UserRecode userRecode = new UserRecode();
            userRecode.setUserId(userId);
            userRecode.setSkuId(skuId);

            //发送消息,将用户和秒杀商品信息放入队列，异步处理
            rabbitService.sendMessage(
                    MqConst.EXCHANGE_DIRECT_SECKILL_USER,
                    MqConst.ROUTING_SECKILL_USER,
                    userRecode);
        }

        return Result.ok();
    }


    /**
     * GET/api/activity/seckill/auth/checkOrder/{skuId}  检查秒杀状态
     *
     * @param skuId
     * @param request
     * @return
     */
    @GetMapping("/auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable Long skuId,
                             HttpServletRequest request) {

        String userId = AuthContextHolder.getUserId(request);

        return seckillGoodsService.checkOrder(userId, skuId);
    }


    /**
     * GET/api/activity/seckill/auth/trade  秒杀下单确认
     *
     * @param request
     * @return
     */
    @GetMapping("/auth/trade")
    public Result trade(HttpServletRequest request) {

        String userId = AuthContextHolder.getUserId(request);

        //验证是否下单，并获取下单数据
        OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);

        if (orderRecode == null) {
            return Result.fail().message("非法请求");
        }

        //获取用户地址
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(Long.valueOf(userId));

        //获取送货信息
        SeckillGoods seckillGoods = orderRecode.getSeckillGoods();

        //转换成前端能识别的数据
        List<OrderDetail> detailArrayList = new ArrayList<>();

        OrderDetail orderDetail = new OrderDetail();

        orderDetail.setSkuId(seckillGoods.getSkuId());
        orderDetail.setSkuName(seckillGoods.getSkuName());
        orderDetail.setImgUrl(seckillGoods.getSkuDefaultImg());
        orderDetail.setOrderPrice(seckillGoods.getPrice());
        orderDetail.setSkuNum(orderRecode.getNum());

        detailArrayList.add(orderDetail);

        //计算总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();

        //封装数据
        Map<Object, Object> resultMap = new HashMap<>();

        resultMap.put("userAddressList", userAddressList);
        resultMap.put("detailArrayList", detailArrayList);
        resultMap.put("totalNum", detailArrayList.size());
        resultMap.put("totalAmount", orderInfo.getTotalAmount());


        return Result.ok(resultMap);
    }


    /**
     * POST/api/activity/seckill/auth/submitOrder  保存秒杀订单到order
     *
     * @param orderInfo
     * @return
     */
    @PostMapping("/auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo,
                              HttpServletRequest request) {

        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.valueOf(userId));

        Long orderId = orderFeignClient.submitOrder(orderInfo);

        if (orderId == null) {
            return Result.fail().message("下单失败请重试");
        }

        //删除下单信息
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).delete(userId);
        //下单记录
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).put(userId, orderId.toString());

        return Result.ok(orderId);
    }

}
