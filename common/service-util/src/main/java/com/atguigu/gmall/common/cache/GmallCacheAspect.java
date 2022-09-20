package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author Aiden
 * @create 2022-09-20 10:01
 */
@Aspect
@Component
public class GmallCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 根据条件的 注解实现分布式锁+缓存
     * <p>
     * 实现思路：
     * 1.首先从缓存中获取数据
     * key--从注解中获取
     * 查到了数据：返回
     * 没有数据：
     * 2.获取锁
     * 没有锁：自旋
     * 有锁
     * 3.查询数据库
     * 有：存储返回
     * 无：存null，返回
     * 4.释放锁
     * 5.兜底方法
     * 执行目标方法的方法体
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @SneakyThrows//抑制异常
    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")//环绕通知，切入点表达式，匹配使用这个注解的方法
    public Object cacheAroundAdvice(ProceedingJoinPoint joinPoint) {
        //创建对象
        Object object = new Object();
        //获取方法签名(将如下信息都获得到了，按需获取即可)
        // @GmallCache (prefix = "sku:" , suffix = ":info")
        //public SkuInfo getSkuInfo(Long skuId){}
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        Object[] args = joinPoint.getArgs();//获取参数

        GmallCache annotation = methodSignature.getMethod().getAnnotation(GmallCache.class);//获取注解信息

        String prefix = annotation.prefix();//获取前缀
        String suffix = annotation.suffix();//获取后缀
        String key = prefix + Arrays.toString(args) + suffix;//拼接key

        Class aClass = methodSignature.getReturnType();//获取当前切到的方法返回值类型

        try {
            //尝试从redis中获取数据（编写了本地方法）
            object = cacheHit(key, aClass);

            if (object == null) {
                //缓存中没有数据
                //获取锁   lockKey = sku:lock
                String lockKey = prefix + RedisConst.LOCK_SUFFIX;
                RLock lock = redissonClient.getLock(lockKey);
                //加锁
                boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);

                if (res) {
                    //获得了锁，查询数据库（环绕通知，执行目标的方法体即可）
                    object = joinPoint.proceed(args);

                    try {
                        //自己写的，当没查到值时返回空的list/map在objec==null时是不成立的，所以进行判断
//                        if (aClass.equals(List.class) && CollectionUtils.isEmpty((Collection)object)) {
//                            redisTemplate.opsForValue().set(key, JSON.toJSONString(object), RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
//                            return object;
//                        }
//
//                        if (aClass.equals(Map.class) && CollectionUtils.isEmpty((Map)object)) {
//                            redisTemplate.opsForValue().set(key, JSON.toJSONString(object), RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
//                            return object;
//                        }
                        if (object == null) {
                            //没有查到，数据库中也没有，返回一个null
                            object = aClass.newInstance();//使用aClass字节码来创建一个object（套了衣服的object），返回给调用者不报错
                            redisTemplate.opsForValue().set(key, JSON.toJSONString(object), RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                            return object;
                        } else {
                            //查到了，存入redis并返回值
                            redisTemplate.opsForValue().set(key, JSON.toJSONString(object), RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                            return object;
                        }
                    } finally {
                        //释放锁
                        lock.unlock();
                    }

                } else {
                    //没有获得锁，自旋
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return cacheAroundAdvice(joinPoint);
                }
            } else {
                //缓存中有数据直接返回
                return object;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        //兜底方法
        return joinPoint.proceed(args);
    }

    private Object cacheHit(String key, Class aClass) {
        //我们向redis中存储也存储string类型，所以以string类型接受，
        String jsonStr = (String) redisTemplate.opsForValue().get(key);

        //判断是否为空
        if (!StringUtils.isEmpty(jsonStr)) {
            Object object = JSONObject.parseObject(jsonStr, aClass);//转换为对应类型的objec
            return object;
        }
        return null;
    }

}
