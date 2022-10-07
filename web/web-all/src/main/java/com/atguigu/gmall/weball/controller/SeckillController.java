package com.atguigu.gmall.weball.controller;

import com.atguigu.gmall.activity.client.ActivityFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Aiden
 * @create 2022-10-06 16:28
 */
@Controller
@SuppressWarnings("all")
public class SeckillController {

    @Autowired
    private ActivityFeignClient activityFeignClient;

    @Autowired
    private OrderFeignClient orderFeignClient;


    /**
     * 秒杀主页
     *
     * @param model
     * @return
     */
    @GetMapping("/seckill.html")
    public String index(Model model) {

        Result result = activityFeignClient.findAll();

        model.addAttribute("list", result.getData());
        return "seckill/index";
    }


    /**
     * 秒杀商品详情页
     *
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/seckill/{skuId}.html")
    public String item(@PathVariable String skuId, Model model) {
        Result result = activityFeignClient.getSeckillGoods(skuId);

        model.addAttribute("item", result.getData());

        return "seckill/item";
    }

    //http://activity.gmall.com/seckill/queue.html?skuId=28&skuIdStr=c4ca4238a0b923820dcc509a6f75849b

    /**
     * 秒杀商品流程页
     *
     * @param request
     * @return
     */
    @GetMapping("/seckill/queue.html")
    public String queue(HttpServletRequest request) {

        String skuId = request.getParameter("skuId");
        String skuIdStr = request.getParameter("skuIdStr");

        //存储
        request.setAttribute("skuId", skuId);
        request.setAttribute("skuIdStr", skuIdStr);

        return "seckill/queue";
    }


    /**
     * 秒杀确认页
     *
     * @return
     */
    @GetMapping("/seckill/trade.html")
    public String trade(Model model) {

        //地址列表 秒杀清单  总数量  总金额
        Result<Map<String, Object>> result = activityFeignClient.trade();

        if (result.isOk()) {
            model.addAllAttributes(result.getData());
            return "seckill/trade";
        }else {
            //失败了，获取失败原因    跳转失败页面
            model.addAttribute("message",result.getMessage());
            return "seckill/fail";
        }
    }


}
