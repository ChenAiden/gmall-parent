package com.atguigu.gmall.activity.config;

import com.atguigu.gmall.activity.util.CacheHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Aiden
 * @create 2022-10-06 15:49
 */
@Component
public class MessageReceiver {


    public void receiverMessage(String message) {

        if (!StringUtils.isEmpty(message)){
            //调换外层引号""21:1""
            message = message.replace("\"", "");
            //截取
            String[] split = message.split(":");

            if (split != null && split.length == 2){
                //存储状态为到本地内存中
                CacheHelper.put(split[0], split[1]);
            }
        }
    }

}
