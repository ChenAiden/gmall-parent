package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoManager;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * @author Aiden
 * @create 2022-09-26 14:22
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoManager userInfoManager;

    @Override
    public UserInfo login(UserInfo userInfo) {

        String passwd = userInfo.getPasswd();
//        String encrypt = MD5.encrypt(passwd);
        String md5Passwd = DigestUtils.md5DigestAsHex(passwd.getBytes());

        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("login_name",userInfo.getLoginName());
        wrapper.eq("passwd",md5Passwd);
        //查询验证用户登陆信息时候正确
        return userInfoManager.selectOne(wrapper);
    }
}
