package com.atguigu.gmall.task.scheduled;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Aiden
 * @create 2022-10-06 14:35
 */
@Component
@EnableScheduling
public class ScheduledTask {

    @Autowired
    private RabbitService rabbitService;

    /**
     * 凌晨一点，数据进入缓存库
     * 添加定时任务
     * 时间表达式
     * * * * * * * *
     * 秒 分 时 日 月 星期 年（年可以不写）
     */
//    @Scheduled(cron = "0/5 * * * * ?")
//    @Scheduled(cron = "0 0 1 * * ? ")
    public void Task_1() {

        System.out.println("凌晨一点的定时器");

        rabbitService.sendMessage(
                MqConst.EXCHANGE_DIRECT_TASK,
                MqConst.ROUTING_TASK_1,
                "");
    }


    /**
     * 18点，清楚redis中无用的秒杀数据
     */
    @Scheduled(cron = "0/5 * * * * ?")
//    @Scheduled(cron = "0 0 18 * * ?")
    public void Task_18() {
        System.out.println("傍晚18点的定时器");

        rabbitService.sendMessage(
                MqConst.EXCHANGE_DIRECT_TASK,
                MqConst.ROUTING_TASK_18,
                "");
    }


}
