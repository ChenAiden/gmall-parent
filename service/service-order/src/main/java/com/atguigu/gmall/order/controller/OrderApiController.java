package com.atguigu.gmall.order.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.StringUtils;
import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author Aiden
 * @create 2022-09-29 10:23
 */
@RestController
@RequestMapping("/api/order")
@SuppressWarnings("all")
public class OrderApiController {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private ThreadPoolExecutor executor;


    //GET/api/order/auth/trade  去结算
    @ApiOperation("去结算")
    @GetMapping("/auth/trade")
    public Result trade(HttpServletRequest request) {
        HashMap<String, Object> resultMap = new HashMap<>();

        //获取userId后，查找用户地址列表
        String userId = AuthContextHolder.getUserId(request);
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(Long.valueOf(userId));


        //得到购物车中选中商品列表
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
        //转换为订单详情
        List<OrderDetail> detailArrayList = cartCheckedList.stream().map(cartInfo -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cartInfo, orderDetail);

            //更新价格
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            return orderDetail;
        }).collect(Collectors.toList());


        //总金额,orderInfo中封装了方法可以使用
        OrderInfo orderInfo = new OrderInfo();
        //传入对象，调用方法，获取值
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();
        BigDecimal totalAmount = orderInfo.getTotalAmount();


        //封装数据
        resultMap.put("userAddressList", userAddressList);//用户地址
        resultMap.put("detailArrayList", detailArrayList);//
        resultMap.put("totalNum", detailArrayList.size());
        resultMap.put("totalAmount", totalAmount);
        //添加流水号
        resultMap.put("tradeNo", orderInfoService.getTradeNo(userId));

        return Result.ok(resultMap);
    }


    //POST/api/order/auth/submitOrder   提交订单
    @ApiOperation("提交订单")
    @PostMapping("/auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo,
                              HttpServletRequest request) {

        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.valueOf(userId));

        //校验流水号是否正确
        String tradeNo = request.getParameter("tradeNo");
        boolean flag = orderInfoService.checkedTradeNo(userId, tradeNo);

        if (!flag) {
            return Result.fail().message("订单不能重复提交");
        }


        //定义错误收集错误的容器
        List<String> errorList = new ArrayList<>();

        //定义收集异步对象的集合
        List<CompletableFuture> futureList = new ArrayList<>();

        //遍历选中商品，校验库存，比较价格
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {

            Long skuId = orderDetail.getSkuId();
            Integer skuNum = orderDetail.getSkuNum();

            //库存查询异步
            CompletableFuture<Void> checkStockFuture = CompletableFuture.runAsync(() -> {

                //调用校验方法
                boolean falg = orderInfoService.checkStock(skuId, skuNum);
                if (!falg) {
                    errorList.add("商品" + orderDetail.getSkuName() + ",库存不足");
//                    return Result.fail().message("商品" + orderDetail.getSkuName() + ",库存不足");
                }

            }, executor);
            futureList.add(checkStockFuture);

            //价格比较异步
            CompletableFuture<Void> CartCheckedFuture = CompletableFuture.runAsync(() -> {
                //再次获取实时价格更新
                BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
                //比较价格
                if (skuPrice.compareTo(orderDetail.getOrderPrice()) != 0) {

                    //比较发现与生成订单时的价格不一致了，更新redis中的价格，以便能实时显示最新的价格
                    cartFeignClient.getCartCheckedList(userId);
                    errorList.add("商品" + orderDetail.getSkuName() + ",价格有变动");
//                    return Result.fail().message("商品" + orderDetail.getSkuName() + ",价格有变动");
                }
            }, executor);
            futureList.add(CartCheckedFuture);
        }

        //异步编排组合
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();

        //错误处理
        if (errorList.size() > 0) {
            //将错误消息拼接起来，统一发送
            return Result.fail().message(StringUtils.join(errorList, ","));
        }


        //提交订单
        Long orderId = orderInfoService.submitOrder(orderInfo);

        //生成订单后，删除流水号
        orderInfoService.deleteTradeNo(userId);

        return Result.ok(orderId);
    }

    //GET/api/order/auth/{page}/{limit} 我的订单
    @GetMapping("/auth/{page}/{limit}")
    public Result getOrderByPage(@PathVariable Long page,
                                 @PathVariable Long limit,
                                 HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);

        Page<OrderInfo> orderInfoPage = new Page<>(page, limit);

        IPage<OrderInfo> orderInfoIPage = orderInfoService.getOrderByPage(orderInfoPage, userId);

        return Result.ok(orderInfoIPage);
    }


    /**
     * 根据id查询orderInfo对象，提供给支付页面显示
     * GET/api/order/inner/getOrderInfo/{orderId}
     *
     * @param orderId
     * @return
     */
    @GetMapping("/inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId) {
        return orderInfoService.getOrderInfoById(orderId);
    }


    //POST/api/order/orderSplit  拆单接口
    @PostMapping("/orderSplit")
    public String orderSplit(HttpServletRequest request){

        //获取订单
        String orderId = request.getParameter("orderId");
        //获取仓库编号和商品的对照关系
        String wareSkuMap = request.getParameter("wareSkuMap");//[{"wareId":"1","skuIds":["28"]},{"wareId":"2","skuIds":["30"]}]

        //拆单
        List<OrderInfo> orderInfoList = orderInfoService.orderSplit(orderId,wareSkuMap);

        List<Map> resultList = new ArrayList<>();

        //遍历集合  将orderInfo转换为map（因为人家要的信息的名字和info的属性名不同）
        for (OrderInfo orderInfo : orderInfoList) {
            Map<String, Object> map = orderInfoService.getStringObjectMap(orderInfo);
            resultList.add(map);
        }

        return JSON.toJSONString(resultList);
    }



}
