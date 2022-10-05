package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Aiden
 * @create 2022-10-04 9:42
 */
//延迟插件实现延迟队列配置类
@Configuration
public class DelayedMqConfig {

    // 声明一些变量
    public static final String exchange_delay = "exchange.delay";
    public static final String routing_delay = "routing.delay";
    public static final String queue_delay_1 = "queue.delay.1";

    @Bean
    public Queue delayQeue1() {

        //参数有自动默认配置
        return new Queue(queue_delay_1);
    }


    //创建定制交换机交换机
    @Bean
    public CustomExchange delayExchange1() {

        Map<String, Object> arguments = new HashMap<>();

        //指定创建什么类型的交换机   定制类型
        arguments.put("x-delayed-type", "direct");

        return new CustomExchange(exchange_delay, "x-delayed-message", true, false, arguments);
    }


    @Bean
    public Binding delayBbinding1() {

        return BindingBuilder.bind(delayQeue1()).to(delayExchange1()).with(routing_delay).noargs();
    }
}
