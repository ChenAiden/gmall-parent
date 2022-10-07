package com.atguigu.gmall.activity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * @author Aiden
 * @create 2022-10-06 15:43
 */
@Configuration
public class RedisChannelConfig {


    /*
         docker exec -it  bc92 redis-cli
         subscribe seckillpush // 订阅 接收消息
         publish seckillpush admin // 发布消息
     */

    /**
     * 注入订阅主题 订阅主题对象
     *
     * @param redisConnectionFactory redis 链接工厂
     * @param messageListenerAdapter 消息监听适配器
     * @return 订阅主题对象
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
                                                                       MessageListenerAdapter messageListenerAdapter) {
        //创建redis消息监听容器
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        //设置连接工厂
        container.setConnectionFactory(redisConnectionFactory);

        //设置主题,设置监听器
        container.addMessageListener(messageListenerAdapter, new PatternTopic("seckillPush"));

        //这个container 可以添加多个 messageListener

        return container;
    }

    /**
     * 创建接收消息对象  返回消息监听器
     *
     * @param receiver
     * @return 返回消息监听器
     */
    @Bean
    public MessageListenerAdapter messageListenerAdapter(MessageReceiver receiver) {

        /**
         * 如何实现反射：
         * 1.通过对象获取字节码对象
         * 2.通过反射执行方法
         */
        //这个地方 是给 messageListenerAdapter 传入一个消息接受的处理器，利用反射的方法调用“receiveMessage”
        //也有好几个重载方法，这边默认调用处理器的方法 叫handleMessage 可以自己到源码里面看
        return new MessageListenerAdapter(receiver, "receiverMessage");
    }


    //注入操作数据的  template
    @Bean
    public StringRedisTemplate template(RedisConnectionFactory redisConnectionFactory) {

        return new StringRedisTemplate(redisConnectionFactory);
    }


}
