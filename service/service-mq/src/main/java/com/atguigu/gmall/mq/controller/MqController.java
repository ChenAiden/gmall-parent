package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Aiden
 * @create 2022-09-30 15:10
 */
@RestController
@RequestMapping("/mq")
public class MqController {

    @Autowired
    private RabbitService rabbitService;

    @GetMapping("/sendMsg")
    public Result sendMsg(){

        rabbitService.sendMessage("exchange.confirm","routing.confirm","我是生产者发送的消息");
        return Result.ok("消息以发送...");
    }
}
