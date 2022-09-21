package com.atguigu.gmall.item.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Aiden
 * @create 2022-09-21 9:42
 */
@Configuration
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor executor() {
        /*
        核心线程数
        最大线程数
        线程空闲时间
        时间单位
        阻塞队列
        创建线程的工厂
        拒绝策略
         */
        return new ThreadPoolExecutor(
                50,
                500,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10000)
        );
    }
}
