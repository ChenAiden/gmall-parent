package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Aiden
 * @create 2022-09-30 15:10
 */
@RestController
@RequestMapping("/mq")
public class MqController {

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @GetMapping("/sendMsg")
    public Result sendMsg() {

        rabbitService.sendMessage("exchange.confirm", "routing.confirm", "我是生产者发送的消息");
        return Result.ok("消息以发送...");
    }

    /**
     * 普通的死信队列，达到延迟消息
     * @return
     */
    @GetMapping("/deadQueueMsg")
    public Result deadQueueMsg() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        rabbitService.sendMessage(
                DeadLetterMqConfig.exchange_dead,
                DeadLetterMqConfig.routing_dead_1,
                "我是死信队列消息");

        System.out.println(sdf.format(new Date()) + " Delay sent.");
        return Result.ok();
    }


    /**
     * 延迟插件
     * @return
     */
    @GetMapping("/sendDelay")
    public Result sendDelay() {

        //改装好了以后使用rabbitService发送
        rabbitService.sendDelayMsg(
                DelayedMqConfig.exchange_delay,
                DelayedMqConfig.routing_delay,
                "我是延迟插件发送的消息",
                10);

        //改装之前的发送方式
//        rabbitTemplate.convertAndSend(
//                DelayedMqConfig.exchange_delay,
//                DelayedMqConfig.routing_delay,
//                "我是延迟插件发送的消息",
//
//                message -> {
//                    //指定延迟时间
//                    message.getMessageProperties().setDelay(10 * 1000);
//
//                    //查看发送时间
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    System.out.println(sdf.format(new Date()) + " Delay sent.");
//                    System.out.println("我发送的消息是：\t" + message);
//
//                    return message;
//                });

        return Result.ok();
    }


}
