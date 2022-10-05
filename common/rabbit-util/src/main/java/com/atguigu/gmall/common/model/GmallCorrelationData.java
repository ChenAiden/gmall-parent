package com.atguigu.gmall.common.model;

import lombok.Data;
import org.springframework.amqp.rabbit.connection.CorrelationData;

/**
 * 自定义一个实体类来接收消息，因为ConfirmCallback需要一个CorrelationData对象
 *
 * @author Aiden
 * @create 2022-09-30 15:37
 */
@Data
public class GmallCorrelationData extends CorrelationData {

    //  消息主体
    private Object message;
    //  交换机
    private String exchange;
    //  路由键
    private String routingKey;
    //  重试次数
    private int retryCount = 0;
    //  消息类型  是否是延迟消息
    private boolean isDelay = false;
    //  延迟时间
    private int delayTime = 10;

}
