package com.atguigu.gmall.order.controller;

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
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
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

        //校验库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {

            Long skuId = orderDetail.getSkuId();
            Integer skuNum = orderDetail.getSkuNum();

            //调用校验方法
            boolean falg = orderInfoService.checkStock(skuId, skuNum);
            if (!falg) {
                return Result.fail().message("商品" + orderDetail.getSkuName() + ",库存不足");
            }


            //再次获取实时价格更新
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            //比较价格
            if (skuPrice.compareTo(orderDetail.getOrderPrice()) != 0) {

                //比较发现与生成订单时的价格不一致了，更新redis中的价格，以便能实时显示最新的价格
                cartFeignClient.getCartCheckedList(userId);

                return Result.fail().message("商品" + orderDetail.getSkuName() + ",价格有变动");
            }
        }


        //提交订单
        Long orderId = orderInfoService.submitOrder(orderInfo);

        //生成订单后，删除流水号
        orderInfoService.deleteTradeNo(userId);

        return Result.ok(orderId);
    }


}
