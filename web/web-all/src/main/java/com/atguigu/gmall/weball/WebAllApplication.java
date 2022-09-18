package com.atguigu.gmall.weball;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Aiden
 * @create 2022-09-17 11:36
 */
@EnableFeignClients(basePackages = "com.atguigu.gmall")
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.atguigu.gmall")
@SpringBootApplication
public class WebAllApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebAllApplication.class,args);
    }
}
