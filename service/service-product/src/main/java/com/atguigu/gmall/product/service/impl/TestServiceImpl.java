package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Aiden
 * @create 2022-09-19 9:08
 */
@Service
public class TestServiceImpl implements TestService {

    @Autowired
//    private RedisTemplate redisTemplate;
    private StringRedisTemplate redisTemplate;


    @Autowired
    private RedissonClient redissonClient;

    /**
     * 演示redisson分布式锁
     * 获取锁：
     */
    @Override
    public void testLock() {
        //使用redisson实现分布式锁
        String lockKey = "sku" + 21 + "info";
        RLock lock = redissonClient.getLock(lockKey);

        //加锁
        lock.lock();

//        lock.lock(3, TimeUnit.SECONDS);

        String numStr = redisTemplate.opsForValue().get("num");

        int num = Integer.parseInt(numStr);
        redisTemplate.opsForValue().set("num", String.valueOf(++num));

        lock.unlock();
    }




    @Override
    public String readLock() {

        RReadWriteLock myLock = redissonClient.getReadWriteLock("myLock");
        RLock rLock = myLock.readLock();

        rLock.lock(10, TimeUnit.SECONDS);

        String msg = redisTemplate.opsForValue().get("msg");

        return "读取redis中的" + msg;
    }

    @Override
    public String writeLock() {
        //生成数据
        RReadWriteLock myLock = redissonClient.getReadWriteLock("myLock");
        RLock rLock = myLock.writeLock();

        String msg = UUID.randomUUID().toString().replace("-", "");

        rLock.lock(10, TimeUnit.SECONDS);

        redisTemplate.opsForValue().set("msg",msg);

        return "向redis中写入" + msg;
    }



//    @Override
//    public synchronized void testLock() {
//        //获取num
//        String numStr = redisTemplate.opsForValue().get("num");
//
//        //判断那
//        if (StringUtils.isEmpty(numStr)) {
//            return;
//        }
//
//        //转换
//        int num = Integer.parseInt(numStr);
////        AtomicInteger num = new AtomicInteger(Integer.parseInt(numStr));
//
//
//        //自增更新
////        ++num;
//        redisTemplate.opsForValue().set("num",String.valueOf(++num));//211 //加synchronized关键字后没问题了
////        redisTemplate.opsForValue().set("num",String.valueOf(num.incrementAndGet()));//227
//
//    }


//    @Override
//    public void testLock() {
//
//        //获取随机锁值
//        String uuid = UUID.randomUUID().toString().replace("-", "");
//
//        //1. 从redis中获取锁,setnx
//        Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", uuid,7, TimeUnit.SECONDS);//添加超时时间，防止突然错误，系统被锁住
//
////        redisTemplate.expire("lock",7, TimeUnit.SECONDS);//与上面操作没有原子性，可能锁设置了，但是过期时间没有设置
//
//        if (flag) {
//
//            //获取num
//            String numStr = redisTemplate.opsForValue().get("num");
//
//            //判断null
//            if (StringUtils.isEmpty(numStr)) {
//                return;
//            }
//
//            //转换
//            int num = Integer.parseInt(numStr);
//
//            //自增更新
//            redisTemplate.opsForValue().set("num", String.valueOf(++num));
//
//            //判断是否是自己的锁
////            if (uuid.equals(redisTemplate.opsForValue().get("lock"))){
////                //释放锁 del
////                redisTemplate.delete("lock");
////            }
//
//            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//
//            //创建脚本对象
//            DefaultRedisScript redisScript = new DefaultRedisScript();
//            //设置
//            redisScript.setResultType(Long.class);
//            //设置脚本
//            redisScript.setScriptText(script);
//
//            redisTemplate.execute(redisScript, Arrays.asList("lock"),uuid);
//
//
//        } else {
//            //没有获得锁-自旋
//            try {
//                Thread.sleep(100);
//                testLock();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//        }
//
//    }
}
