package com.atguigu.gmall.weball.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @author Aiden
 * @create 2022-09-17 11:40
 */
@Controller
public class ItemController {

    @Autowired
    private ItemFeignClient itemFeignClient;

    @RequestMapping("/{skuId}.html")
    public String item(@PathVariable Long skuId, Model model){

        Result<Map<String,Object>> item = itemFeignClient.getItem(skuId);

        model.addAllAttributes(item.getData());
        return "item/item";
    }



}
