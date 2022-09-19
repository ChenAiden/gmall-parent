package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Aiden
 * @create 2022-09-19 9:06
 */
@RequestMapping("admin/product/test")
@RestController
public class TestController {

    @Autowired
    private TestService testService;


    @GetMapping("testLock")
    public Result testLock(){

        testService.testLock();
        return Result.ok();
    }

    @GetMapping("read")
    public Result read(){

        String read = testService.readLock();
        return Result.ok(read);
    }

    @GetMapping("write")
    public Result write(){

        String write = testService.writeLock();
        return Result.ok(write);
    }
}
