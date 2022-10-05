package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Aiden
 * @create 2022-10-04 16:28
 */
@Service
@SuppressWarnings("all")
public class AlipayServiceImpl implements AlipayService {


    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private AlipayClient alipayClient;

    @Override
    public String submit(Long orderId) {
        //判断订单状态
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);

        if ("PAID".equals(orderInfo.getOrderStatus()) || "CLOSE".equals(orderInfo.getOrderStatus())){
            return "该订单已经完成或已经关闭!";
        }


        //支付宝接口的写法
//        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
//        request.setNotifyUrl("");
        request.setReturnUrl(AlipayConfig.return_payment_url);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderInfo.getOutTradeNo());
        bizContent.put("total_amount", 0.01);
        bizContent.put("subject", orderInfo.getTradeBody());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        //超时绝对时间
        //bizContent.put("time_expire", "2022-08-01 22:00:00");
        //  设置二维码过期时间
        bizContent.put("timeout_express","10m");


        //// 商品明细信息，按需传入
        //JSONArray goodsDetail = new JSONArray();
        //JSONObject goods1 = new JSONObject();
        //goods1.put("goods_id", "goodsNo1");
        //goods1.put("goods_name", "子商品1");
        //goods1.put("quantity", 1);
        //goods1.put("price", 0.01);
        //goodsDetail.add(goods1);
        //bizContent.put("goods_detail", goodsDetail);

        //// 扩展信息，按需传入
        //JSONObject extendParams = new JSONObject();
        //extendParams.put("sys_service_provider_id", "2088511833207846");
        //bizContent.put("extend_params", extendParams);


        request.setBizContent(bizContent.toString());
        AlipayTradePagePayResponse response = null;

        try {
            response = alipayClient.pageExecute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if(response.isSuccess()){
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }

        return response.getBody();
    }


}
