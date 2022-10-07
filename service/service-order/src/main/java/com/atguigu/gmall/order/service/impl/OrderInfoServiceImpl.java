package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author Aiden
 * @create 2022-09-29 11:31
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${ware.url}")
    private String wareUrl;

    @Autowired
    private RabbitService rabbitService;


    @Transactional
    @Override
    public Long submitOrder(OrderInfo orderInfo) {

        //保存订单
        //补全orderInfo的信息
        orderInfo.sumTotalAmount();//计算出总金额，并保存
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        orderInfo.setPaymentWay(PaymentType.ALIPAY.name());
        //订单交易编号
        String outTradeNo = "guigu" + System.currentTimeMillis() + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);

        //订单描述
        StringBuilder builder = new StringBuilder();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            builder.append(orderDetail.getSkuName());
        }
        //设置订单描述
        if (builder.toString().length() >= 100) {
            String substring = builder.toString().substring(100);
            orderInfo.setTradeBody(substring);
        } else {
            orderInfo.setTradeBody(builder.toString());
        }
        //操作时间
        orderInfo.setOperateTime(new Date());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        Date time = calendar.getTime();
        //失效时间
        orderInfo.setExpireTime(time);

        //进度状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());


        //保存订单
        orderInfoMapper.insert(orderInfo);

        //保存订单明细
        Long orderId = orderInfo.getId();

        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderId);
            orderDetailMapper.insert(orderDetail);

            //订单保存完毕后删除购物车中的内容，保存一个删除一个
//            redisTemplate.boundHashOps(RedisConst.USER_KEY_PREFIX + orderInfo.getUserId() + RedisConst.USER_CART_KEY_SUFFIX).delete(orderDetail.getSkuId());
        }


        //开始计时，超时则取消订单
        rabbitService.sendDelayMsg(
                MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,
                MqConst.ROUTING_ORDER_CANCEL,
                orderId,
                MqConst.DELAY_TIME);

        return orderId;
    }

    /**
     * 获取流水号
     *
     * @param userId
     * @return
     */
    @Override
    public String getTradeNo(String userId) {
        //生成流水号
        String tradeNo = UUID.randomUUID().toString().replace("-", "");

        //存入redis
        String tradeNoKey = "user:" + userId + ":tradeno";
        redisTemplate.opsForValue().set(tradeNoKey, tradeNo);

        return tradeNo;
    }

    /**
     * 获取流水号进行校验
     *
     * @param userId
     * @param tradeNo
     * @return
     */
    @Override
    public boolean checkedTradeNo(String userId, String tradeNo) {
        if (StringUtils.isEmpty(tradeNo)) {
            return false;
        }

        String tradeNoKey = "user:" + userId + ":tradeno";

        String redisTradeNo = (String) redisTemplate.opsForValue().get(tradeNoKey);
        return tradeNo.equals(redisTradeNo);
    }

    /**
     * 删除流水号
     *
     * @param userId
     */
    @Override
    public void deleteTradeNo(String userId) {
        String tradeNoKey = "user:" + userId + ":tradeno";
        redisTemplate.delete(tradeNoKey);
    }


    /**
     * 调用库存系统接口查询是否还有库存
     *
     * @param skuId
     * @param skuNum
     * @return
     */
    @Override
    public boolean checkStock(Long skuId, Integer skuNum) {
        // 远程调用http://localhost:9001/hasStock?skuId=10221&num=2
        String url = wareUrl + "/hasStock?skuId=" + skuId + "&num=" + skuNum;
        //远程调用库存系统
        String result = HttpClientUtil.doGet(url);

        return "1".equals(result);
    }

    @Override
    public IPage<OrderInfo> getOrderByPage(Page<OrderInfo> orderInfoPage, String userId) {

        IPage<OrderInfo> orderInfoIPage = orderInfoMapper.selectOrderByPage(orderInfoPage, userId);

        List<OrderInfo> records = orderInfoIPage.getRecords();
        records.stream().forEach(orderInfo -> {
            //获取状态
            String orderStatus = orderInfo.getOrderStatus();
            //修改状态名
            orderInfo.setOrderStatusName(OrderStatus.getStatusNameByStatus(orderStatus));
        });

        return orderInfoIPage;
    }


    /**
     * 关闭订单
     *
     * @param orderId
     */
    @Override
    public void cancelOrder(Long orderId,String falg) {

//        OrderInfo orderInfo = new OrderInfo();
//        orderInfo.setId(orderId);
//        //订单状态
//        orderInfo.setOrderStatus(OrderStatus.CLOSED.name());
//        //步骤状态
//        orderInfo.setProcessStatus(ProcessStatus.CLOSED.name());

//        orderInfoMapper.updateById(orderInfo);

        //封装了方法专门修改当前订单状态
        this.updateOrder(orderId, ProcessStatus.CLOSED);


        if (falg.equals("2")){

            //修改payment状态
            rabbitService.sendMessage(
                    MqConst.EXCHANGE_DIRECT_PAYMENT_CLOSE,
                    MqConst.ROUTING_PAYMENT_CLOSE,
                    orderId);
        }
    }

    @Override
    public void updateOrder(Long orderId, ProcessStatus processStatus) {

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        //订单状态
        orderInfo.setOrderStatus(processStatus.getOrderStatus().name());
        //步骤状态
        orderInfo.setProcessStatus(processStatus.name());

        orderInfoMapper.updateById(orderInfo);
    }

    /**
     * 根据id查询orderInfo对象，提供给支付页面显示
     *
     * @param orderId
     * @return
     */
    @Override
    public OrderInfo getOrderInfoById(Long orderId) {
        //查询订单
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        //根据orderId查询  订单详情
        QueryWrapper<OrderDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(wrapper);

        orderInfo.setOrderDetailList(orderDetails);

        return orderInfo;
    }

    @Override
    public void sendOrderSatusToSotck(Long orderId) {
        //查询订单消息
        OrderInfo orderInfo = getOrderInfoById(orderId);

        //修改订单状态,改为已经通知仓储
        updateOrder(orderId,ProcessStatus.NOTIFIED_WARE);

        //转化库存系统需要的数据 json
        String mapJson = initWareOrder(orderInfo);

        //发送消息
        rabbitService.sendMessage(
                MqConst.EXCHANGE_DIRECT_WARE_STOCK,
                MqConst.ROUTING_WARE_STOCK,
                mapJson);
    }

    /**
     * 拆单：
     * 原来的订单根据库存地址不同，拆分为多个订单
     *      orderInfo--来自父订单
     *      orderDetail--根据对照关系从父的详情集合中获取对应的详情对象
     *
     * [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
     *
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    @Override
    public List<OrderInfo> orderSplit(String orderId, String wareSkuMap) {
        //查询父订单
        OrderInfo orderInfo = getOrderInfoById(Long.valueOf(orderId));

        //定义集合收集子订单
        List<OrderInfo> orderInfoList = new ArrayList<>();

        //处理根据
        List<Map> mapList = JSONArray.parseArray(wareSkuMap, Map.class);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (Map map : mapList) {
            //封装 子OrderInfo
            OrderInfo subOrderInfo = new OrderInfo();
            //拷贝数据
            BeanUtils.copyProperties(orderInfo,subOrderInfo);
            //设置仓库id
            subOrderInfo.setWareId((String) map.get("wareId"));
            //置空id 防止主键冲突
            subOrderInfo.setId(null);


            //获取当前仓库对应的skuId
            List<String> ids = (List<String>) map.get("skuIds");

            //封装orderDetail
            List<OrderDetail> subOrderDetailList = new ArrayList<>();
            //遍历父订单的订单详情
            for (OrderDetail orderDetail : orderDetailList) {
                for (String id : ids) {
                    if (orderDetail.getSkuId().toString().equals(id)){
                        subOrderDetailList.add(orderDetail);
                    }
                }
            }

            subOrderInfo.setOrderDetailList(subOrderDetailList);

            //总金额
            subOrderInfo.sumTotalAmount();

            //保存订单
            this.submitOrder(subOrderInfo);

            //收集子订单
            orderInfoList.add(subOrderInfo);
        }

        //修该父订单状态
        this.updateOrder(Long.valueOf(orderId),ProcessStatus.SPLIT);

        return orderInfoList;
    }

    private String initWareOrder(OrderInfo orderInfo) {
        Map<String, Object> resultMap = getStringObjectMap(orderInfo);

        return JSON.toJSONString(resultMap);
    }

    public Map<String, Object> getStringObjectMap(OrderInfo orderInfo) {
        //创建一个map
        Map<String,Object> resultMap = new HashMap<>();

        resultMap.put("orderId", orderInfo.getId());
        resultMap.put("consignee", orderInfo.getConsignee());
        resultMap.put("consigneeTel", orderInfo.getConsigneeTel());
        resultMap.put("orderComment", orderInfo.getTotalAmount());
        resultMap.put("orderBody", orderInfo.getTradeBody());
        resultMap.put("deliveryAddress", orderInfo.getDeliveryAddress());
        resultMap.put("paymentWay","2");

        //处理仓库
        resultMap.put("wareId",orderInfo.getWareId());

        //创建集合收集明细
        List<Map<String,Object>> details = new ArrayList<>();

        List<OrderDetail> detailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : detailList) {
            Map<String,Object> map = new HashMap<>();
            map.put("skuId",orderDetail.getSkuId());
            map.put("skuNum",orderDetail.getSkuNum());
            map.put("skuName",orderDetail.getSkuName());
            details.add(map);
        }

        resultMap.put("details",details);
        return resultMap;
    }


}
