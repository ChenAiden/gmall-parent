package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 死信队列方式实现延迟消息
 * @author Aiden
 * @create 2022-10-04 9:11
 */
@Configuration
public class DeadLetterMqConfig {

    // 声明一些变量
    public static final String exchange_dead = "exchange.dead";
    public static final String routing_dead_1 = "routing.dead.1";
    public static final String routing_dead_2 = "routing.dead.2";
    public static final String queue_dead_1 = "queue.dead.1";
    public static final String queue_dead_2 = "queue.dead.2";


    /**
     * 创建交换机
     * @return
     */
    @Bean
    public DirectExchange exchange(){
        return new DirectExchange(exchange_dead,true,false,null);
    }

    @Bean
    public Queue queue1(){
        //不要忘记  设置相关参数
        // 设置如果队列一 出现问题，则通过参数转到exchange_dead，routing_dead_2 上！
        Map<String, Object> arguments = new HashMap<>();

        // 参数绑定 此处的key 固定值，不能随意写
        //指定死信交换机
        arguments.put("x-dead-letter-exchange",exchange_dead);
        //指定死信路由
        arguments.put("x-dead-letter-routing-key",routing_dead_2);
        // 设置延迟时间 10s
        arguments.put("x-message-ttl", 10 * 1000);

        //exclusive:是否独享，此队列是否可以被多次使用
        // 队列名称，是否持久化，是否独享、排外的【true:只可以在本次连接中访问】，是否自动删除，队列的其他属性参数
        return new Queue(queue_dead_1,true,false,false,arguments);
    }

    /**
     * 死信队列
     * @return
     */
    @Bean
    public Queue queue2(){

        return new Queue(queue_dead_2,true);
    }

    @Bean
    public Binding binding1(){

        return BindingBuilder.bind(queue1()).to(exchange()).with(routing_dead_1);
    }

    @Bean
    public Binding binding2(){

        return BindingBuilder.bind(queue2()).to(exchange()).with(routing_dead_2);
    }

}
