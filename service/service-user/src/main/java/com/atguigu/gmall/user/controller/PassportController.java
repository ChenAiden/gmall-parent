package com.atguigu.gmall.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Aiden
 * @create 2022-09-26 14:18
 */
@RestController
@RequestMapping("/api/user/passport")
public class PassportController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;


    //POST/api/user/passport/login  登录
    @ApiOperation("登录")
    @PostMapping("/login")
    public Result login(@RequestBody UserInfo userInfo, HttpServletRequest request) {

        UserInfo user = userService.login(userInfo);
        if (user == null) {
            return Result.fail("账号名与密码不匹配，请重新输入");
        }

        //页面需要的数据
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("nickName", user.getNickName());

        String token = UUID.randomUUID().toString().replace("-", "");
        resultMap.put("token", token);


        //认证需要的数据，redis中备份
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", user.getId().toString());
        jsonObject.put("ip", IpUtil.getIpAddress(request));

        redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX + token, jsonObject.toJSONString(),
                RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);

        return Result.ok(resultMap);
    }


    //GET/api/user/passport/logout  退出
    @ApiOperation("退出")
    @GetMapping("/logout")
    public Result logout(HttpServletRequest request) {
        //获取token
        String token = request.getHeader("token");
        //判断有无token
        if (token == null) {
            throw new GmallException("没有接收到token", ResultCodeEnum.FAIL.getCode());
        }

        //删除redis认证信息
        redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX + token);

        return Result.ok();
    }


}
