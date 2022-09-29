package com.atguigu.gmall.user.client;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.client.impl.UserDegradeFeignClient;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author Aiden
 * @create 2022-09-29 10:11
 */
@FeignClient(value = "service-user",fallback = UserDegradeFeignClient.class)
public interface UserFeignClient {

    @ApiOperation("获取用户地址")
    @GetMapping("/api/user/inner/findUserAddressListByUserId/{userId}")
    public List<UserAddress> findUserAddressListByUserId(@PathVariable Long userId);

}
