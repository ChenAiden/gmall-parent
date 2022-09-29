package com.atguigu.gmall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Aiden
 * @create 2022-09-29 10:18
 */
@ComponentScan(basePackages = "com.atguigu.gmall")
@EnableFeignClients(basePackages = "com.atguigu.gmall")
@EnableDiscoveryClient
@SpringBootApplication
public class ServiceOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOrderApplication.class,args);
    }
}
