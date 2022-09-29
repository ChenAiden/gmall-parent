package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Aiden
 * @create 2022-09-27 10:16
 */
@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    @Autowired
    private CartService cartService;

    //GET/api/cart/addToCart/{skuId}/{skuNum}  添加购物车
    @ApiOperation("添加购物车")
    @GetMapping("/addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable Long skuId,
                            @PathVariable Integer skuNum,
                            HttpServletRequest request) {

        //获取用户userId
        String userId = AuthContextHolder.getUserId(request);
        //判断，为空则获取临时id
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }

        cartService.addToCart(userId, skuId, skuNum);
        return Result.ok();
    }

    /**
     * GET/api/cart/cartList  查询购物车列表
     *
     * @return
     */
    @ApiOperation("展示购物车列表")
    @GetMapping("/cartList")
    public Result cartList(HttpServletRequest request) {

        //获取userId
        String userId = AuthContextHolder.getUserId(request);
        //获取临时id
        String userTempId = AuthContextHolder.getUserTempId(request);

        List<CartInfo> cartInfoList = cartService.cartList(userId, userTempId);

        return Result.ok(cartInfoList);
    }

    //DELETE/api/cart/deleteCart/{skuId}  删除购物车
    @DeleteMapping("/deleteCart/{skuId}")
    public Result delectCart(@PathVariable String skuId, HttpServletRequest request) {
        //获取userId
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }

        cartService.delectCart(userId, skuId);

        return Result.ok();
    }

    //GET/api/cart/checkCart/{skuId}/{isChecked}  更新选中状态
    @GetMapping("/checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable String skuId,
                            @PathVariable Integer isChecked,
                            HttpServletRequest request) {
        //获取userId
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }

        cartService.checkCart(userId, skuId, isChecked);

        return Result.ok();
    }


    //GET/api/cart/chooseAll/{isChooseAll}  全选
    @GetMapping("/chooseAll/{isChooseAll}")
    public Result chooseAll(@PathVariable Integer isChooseAll,
                             HttpServletRequest request) {

        //获取userId
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }

        cartService.chooseAll(userId,isChooseAll);
        return Result.ok();
    }

    //GET/api/cart/getCartCheckedList/{userId}  获取选中状态的购物车列表
    @ApiOperation("获取选中状态的购物车列表")
    @GetMapping("/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable String userId){

        return cartService.getCartCheckedList(userId);
    }


}
