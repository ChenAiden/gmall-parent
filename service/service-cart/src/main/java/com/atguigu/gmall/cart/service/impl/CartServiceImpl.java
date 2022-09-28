package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author Aiden
 * @create 2022-09-27 10:18
 */
@Service
@SuppressWarnings("all")
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProductFeignClient productFeignClient;

    /**
     * 存储格式
     * key：表示用户唯一
     * user:1:cart
     * value：购物车列表
     * hash类型
     * key：skuId  value:cartInfo
     *
     * @param userId
     * @param skuId
     * @param skuNum
     */
    @Override
    public void addToCart(String userId, Long skuId, Integer skuNum) {
        //定义存储key
        String cartKey = getCartKey(userId);
        //获取redis中的用户购物车
        BoundHashOperations<String, String, CartInfo> boundHashOps = redisTemplate.boundHashOps(cartKey);

        CartInfo cartInfo = null;
        //判断
        if (boundHashOps.hasKey(skuId.toString())) {
            //说明购物车列表中包含该商品,取出数据，更新数据
            cartInfo = boundHashOps.get(skuId.toString());
            //更新数量
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            //更新时间
            cartInfo.setUpdateTime(new Date());
            //选中状态
            cartInfo.setIsChecked(1);
            //更新实时价格
            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(skuId));

        } else {
            //购物车列表中没有该购物项,添加信息
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            //创建cartinfo`
            cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setUserId(userId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setIsChecked(1);
            cartInfo.setCreateTime(new Date());
            cartInfo.setUpdateTime(new Date());
        }
        boundHashOps.put(skuId.toString(), cartInfo);
    }

    /**
     * 展示购物车 -> 合并购物车
     *
     * @param userId
     * @param userTempId
     * @return
     */
    @Override
    public List<CartInfo> cartList(String userId, String userTempId) {
        List<CartInfo> noLoginCartInfoList = new ArrayList<>();

        //情况一：未登录
        if (!StringUtils.isEmpty(userTempId)) {
            noLoginCartInfoList = redisTemplate.boundHashOps(getCartKey(userTempId)).values();
        }
        //判断userId是否为空
        if (StringUtils.isEmpty(userId)) {
            if (!CollectionUtils.isEmpty(noLoginCartInfoList)) {
                //按时间顺序排序
                noLoginCartInfoList.sort(((o1, o2) -> {
                    return DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND);
                }));
                return noLoginCartInfoList;
            }
        }


        //情况二：登录了
        BoundHashOperations<String, String, CartInfo> boundHashOps = redisTemplate.boundHashOps(getCartKey(userId));

        //判断登陆前临时购物车中是否有数据
        if (!CollectionUtils.isEmpty(noLoginCartInfoList)) {
            //合并
            for (CartInfo cartInfo : noLoginCartInfoList) {
                if (boundHashOps.hasKey(cartInfo.getSkuId().toString())) {
                    //有相同产品--合并
                    CartInfo loginCartInfo = boundHashOps.get(cartInfo.getSkuId().toString());
                    //更新数量
                    loginCartInfo.setSkuNum(loginCartInfo.getSkuNum() + cartInfo.getSkuNum());
                    loginCartInfo.setUpdateTime(new Date());
                    loginCartInfo.setSkuPrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));
                    //更新选中状态
                    if (cartInfo.getIsChecked().intValue() == 1) {
                        loginCartInfo.setIsChecked(1);
                    }

                    //更新数据
                    boundHashOps.put(cartInfo.getSkuId().toString(), loginCartInfo);
                } else {
                    //没有相同产品--合并
                    cartInfo.setUpdateTime(new Date());
                    cartInfo.setSkuPrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));
                    //更新登录后的购物车
                    boundHashOps.put(cartInfo.getSkuId().toString(), cartInfo);
                }
            }
            //删除临时购物车
            redisTemplate.delete(getCartKey(userTempId));
        }

        //不用合并，直接返回登录之后的购物车,合并后的购物车也走这里进行排序
        List<CartInfo> loginCartInfo = boundHashOps.values();

        loginCartInfo.sort((o1, o2) -> {
            return DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND);
        });

        return loginCartInfo;
    }

    @Override
    public void delectCart(String userId, String skuId) {
        String cartKey = getCartKey(userId);

        redisTemplate.boundHashOps(cartKey).delete(skuId);
    }

    @Override
    public void checkCart(String userId, String skuId, Integer isChecked) {
        BoundHashOperations<String, String, CartInfo> boundHashOps = redisTemplate.boundHashOps(getCartKey(userId));

        //获取指定购物项
        CartInfo cartInfo = boundHashOps.get(skuId);

        if (cartInfo != null) {
            cartInfo.setIsChecked(isChecked);
            boundHashOps.put(skuId, cartInfo);
        }
    }

    @Override
    public void chooseAll(String userId, Integer isChooseAll) {
        String cartKey = getCartKey(userId);

        BoundHashOperations<String, String, CartInfo> boundHashOps = redisTemplate.boundHashOps(cartKey);
        Map<String, CartInfo> entries = boundHashOps.entries();
        if (CollectionUtils.isEmpty(entries)){
            for (Map.Entry<String, CartInfo> stringCartInfoEntry : entries.entrySet()) {
                CartInfo cartInfo = stringCartInfoEntry.getValue();

                cartInfo.setIsChecked(isChooseAll);
                stringCartInfoEntry.setValue(cartInfo);
            }
            boundHashOps.putAll(entries);
        }
    }

    //旧版本未合并登录前购物车
//    @Override
//    public List<CartInfo> cartList(String userId, String userTempId) {
//        List<CartInfo> cartInfoList = new ArrayList<>();
//
//        //获取临时id对应的购物车
//        if (!StringUtils.isEmpty(userTempId)) {
//            BoundHashOperations boundHashOps = redisTemplate.boundHashOps(getCartKey(userTempId));
//            cartInfoList = boundHashOps.values();
//        }
//
//        //获取用户对应的购物车
//        if (!StringUtils.isEmpty(userId)) {
//            BoundHashOperations boundHashOps = redisTemplate.boundHashOps(getCartKey(userId));
//            cartInfoList = boundHashOps.values();
//        }
//
//
//        if (!CollectionUtils.isEmpty(cartInfoList)) {
//            //按时间顺序排序
//            cartInfoList.sort((o1, o2) -> {
//                return DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND);
//            });
//        }
//
//        return cartInfoList;
//    }

    private String getCartKey(String userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }
}
