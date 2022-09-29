package com.atguigu.gmall.user.controller.api;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.service.UserAddressService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Aiden
 * @create 2022-09-29 9:29
 */
@RestController
@RequestMapping("/api/user")
public class UserApiController {

    @Autowired
    private UserAddressService userAddressService;

    //GET/api/user/inner/findUserAddressListByUserId/{userId}  获取用户地址
    @ApiOperation("获取用户地址")
    @GetMapping("/inner/findUserAddressListByUserId/{userId}")
    public List<UserAddress> findUserAddressListByUserId(@PathVariable Long userId){

        return userAddressService.findUserAddressListByUserId(userId);
    }

}
