package com.atguigu.gmall.product;

import com.atguigu.gmall.common.constant.RedisConst;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Aiden
 * @create 2022-09-08 14:36
 */
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.atguigu.gmall")
@SpringBootApplication
public class ServiceProductApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(ServiceProductApplication.class,args);
    }

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void run(String... args) throws Exception {
        //获取布隆过滤器
        RBloomFilter<Object> rBloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);

        //初始化参数一：
        rBloomFilter.tryInit(100000,0.01);

    }
}

