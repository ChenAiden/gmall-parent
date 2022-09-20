package com.atguigu.gmall.product.service.impl;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Aiden
 * @create 2022-09-20 15:33
 */
public class CompletableFutureDemo {
    public static void main(String[] args) {

//        ThreadPoolExecutor pool = new ThreadPoolExecutor(3, 5, 5,
//                TimeUnit.SECONDS, new ArrayBlockingQueue(5), new ThreadFactory() {
//            @Override
//            public Thread newThread(Runnable r) {
//                return null;
//            }
//        });

        //创建线程
        //无返回值
//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                //这里放异步函数
//            System.out.println("我没有返回值");
//        }).whenComplete((vo,exec) -> {
//            System.out.println("你好");
//        });


        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println("异步，无返回值");
        }).whenComplete(new BiConsumer<Void, Throwable>() {
            @Override
            public void accept(Void unused, Throwable throwable) {
                System.out.println("throwable:" + throwable);
            }
        }).exceptionally(new Function<Throwable, Void>() {
            @Override
            public Void apply(Throwable throwable) {
                System.out.println("throwable:" + throwable);
                return null;
            }
        });


        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("异步执行");                             //异步执行
            int i = 1/0;
            return 1024;

        }).whenComplete(new BiConsumer<Integer, Throwable>() {
            //回调方法

            @Override
            public void accept(Integer integer, Throwable throwable) {
                System.out.println(integer);
                System.out.println("throwable:" + throwable);
            }
        }).exceptionally(new Function<Throwable, Integer>() {
            //异常处理方法

            @Override
            public Integer apply(Throwable throwable) {
                System.out.println("throwable:" + throwable);
                return 404;
            }
        });


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



    }
}
