package com.atguigu.gmall.weball.controller;

import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Aiden
 * @create 2022-09-27 10:59
 */
@Controller
@SuppressWarnings("all")
public class CartController {

    @Autowired
    private ProductFeignClient productFeignClient;


    @GetMapping("addCart.html")
    public String addCartInfo(@RequestParam Long skuId,
                              @RequestParam Integer skuNum,
                              Model model) {
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        model.addAttribute("skuNum", skuNum);
        model.addAttribute("skuInfo", skuInfo);

        return "cart/addCart";
    }

    @GetMapping("/cart.html")
    public String index() {

        return "cart/index";
    }
}
