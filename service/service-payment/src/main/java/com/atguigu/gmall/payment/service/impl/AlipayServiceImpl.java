package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
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

        if ("PAID".equals(orderInfo.getOrderStatus()) || "CLOSE".equals(orderInfo.getOrderStatus())) {
            return "该订单已经完成或已经关闭!";
        }

        //调用保存交易记录方法！
        paymentService.savePayment(orderInfo, PaymentType.ALIPAY);


        //支付宝接口的写法
//        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        //异步回调地址
        request.setNotifyUrl(AlipayConfig.notify_payment_url);
        //同步回调地址
        request.setReturnUrl(AlipayConfig.return_payment_url);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderInfo.getOutTradeNo());
        bizContent.put("total_amount", 0.01);
        bizContent.put("subject", orderInfo.getTradeBody());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        //超时绝对时间
        //bizContent.put("time_expire", "2022-08-01 22:00:00");
        //  设置二维码过期时间
        bizContent.put("timeout_express", "10m");


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

        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }

        return response.getBody();
    }


    /**
     * 退款
     *
     * @param orderId
     * @return
     */
    @Override
    public boolean refund(Long orderId) {

        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);

        if (orderInfo == null) return false;

//        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderInfo.getOutTradeNo());
        bizContent.put("refund_amount", 0.01);
        bizContent.put("out_request_no", "HZ01RF001");

        //// 返回参数选项，按需传入
        //JSONArray queryOptions = new JSONArray();
        //queryOptions.add("refund_detail_item_list");
        //bizContent.put("query_options", queryOptions);

        request.setBizContent(bizContent.toString());

        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if (response.isSuccess()) {
            System.out.println("调用成功");
            //退款成功后修改订单状态
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setPaymentStatus(PaymentStatus.CLOSED.name());

            paymentService.updatePaymentInfo(orderInfo.getOutTradeNo(), PaymentType.ALIPAY.name(), paymentInfo);

            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }

    }

    /**
     * 查询支付宝交易记录
     * @param orderId
     * @return
     */
    @Override
    public boolean checkPayment(Long orderId) {
        //根据orderId查询对象
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);

        if (orderInfo == null) return false;

//        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderInfo.getOutTradeNo());
        //bizContent.put("trade_no", "2014112611001004680073956707");//可选

        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if (response.isSuccess()) {
            String tradeStatus = response.getTradeStatus();
            if (tradeStatus.equals("WAIT_BUYER_PAY")) {
                //交易状态：WAIT_BUYER_PAY（交易创建，等待买家付款）
                //只有处于等待支付时才返回true
                return true;
            }
            System.out.println("查询交易接口调用成功");
        } else {
            System.out.println("查询交易接口调用失败");
            return false;
        }

        return false;
    }


    /**
     * 查询支付宝支付记录，是否关闭
     *
     * @param orderId
     * @return
     */
    @Override
    public boolean closePay(Long orderId) {
        //根据orderId查询对象
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);

        if (orderInfo == null) return false;

//        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        JSONObject bizContent = new JSONObject();

        bizContent.put("out_trade_no", orderInfo.getOutTradeNo());
//        bizContent.put("trade_no", "2013112611001004680073956707");//不选这个，因为没有回调成功的话没有trade_no

        request.setBizContent(bizContent.toString());
        AlipayTradeCloseResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if(response.isSuccess()){
            System.out.println("查询支付宝支付记录关闭接口，调用成功");
            return true;
        } else {
            System.out.println("查询支付宝支付记录关闭接口，调用失败");
            return false;
        }
    }


}
