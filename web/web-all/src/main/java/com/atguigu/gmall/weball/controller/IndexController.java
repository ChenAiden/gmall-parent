package com.atguigu.gmall.weball.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Aiden
 * @create 2022-09-21 14:21
 */
@Controller
public class IndexController {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * 首页数据显示，渲染
     * @param model
     * @return
     */
    @GetMapping({"/","/index"})
    public String index(Model model){

        Result result = productFeignClient.getBaseCategoryList();
        model.addAttribute("list",result.getData());

        return "index/index";
    }

    /**
     * 第二种实现方式：使用nginx实现动态代理，动静分离
     * @return
     */
    @ResponseBody
    @GetMapping("/createIndex")
    public Result createIndex(){

        //获取首页需要的数据
        Result result = productFeignClient.getBaseCategoryList();

        //创建上下文对象（上下文意味着数据）
        Context context = new Context();
        context.setVariable("list",result.getData());

        //创建流对象
        FileWriter writer = null;
        try {
            writer = new FileWriter("D:\\server\\nginx-1.20.1\\html\\index.html");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //生成页面                           //模版     //数据  //输出位置
        templateEngine.process("index/index",context,writer);

        return Result.ok();
    }
}
