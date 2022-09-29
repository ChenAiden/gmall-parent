package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;

import java.util.List;

/**
 * @author Aiden
 * @create 2022-09-26 14:22
 */
public interface UserAddressService {

    List<UserAddress> findUserAddressListByUserId(Long userId);
}
