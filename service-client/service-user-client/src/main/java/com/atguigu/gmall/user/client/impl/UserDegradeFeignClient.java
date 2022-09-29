package com.atguigu.gmall.user.client.impl;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Aiden
 * @create 2022-09-29 10:12
 */
@Component
public class UserDegradeFeignClient implements UserFeignClient {

    @Override
    public List<UserAddress> findUserAddressListByUserId(Long userId) {
        return null;
    }
}
