package com.atguigu.gmall.list;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Aiden
 * @create 2022-09-23 9:11
 */
@EnableFeignClients(basePackages = "com.atguigu.gmall")
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.atguigu.gmall")
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ServiceListApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceListApplication.class,args);
    }
}
