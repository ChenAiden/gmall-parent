package com.atguigu.gmall.product.service.impl;

import java.util.concurrent.*;

/**
 * @author Aiden
 * @create 2022-09-20 15:33
 */
public class CompletableFutureDemo {
    public static void main(String[] args) {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                10,
                30,
                20,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue(1000));

        //创建一个有返回值的异步对象
        CompletableFuture<String> futureA = CompletableFuture.supplyAsync(() -> {
            System.out.println("我是A，我先执行");
            return "我是A，我先执行";
        },executor);

        CompletableFuture<Void> futureB = futureA.thenAcceptAsync(msg -> {
            System.out.println("我是B，来自A的结果：" + msg);
        },executor);

        CompletableFuture<Void> futureC = futureA.thenAcceptAsync(msg -> {
            System.out.println("我是C，来自A的结果：" + msg);
        },executor);


        //创建线程
        //无返回值
//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                //这里放异步函数
//            System.out.println("我没有返回值");
//        }).whenComplete((vo,exec) -> {
//            System.out.println("你好");
//        });


//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//            System.out.println("异步，无返回值");
//        }).whenComplete(new BiConsumer<Void, Throwable>() {
//            @Override
//            public void accept(Void unused, Throwable throwable) {
//                System.out.println("throwable:" + throwable);
//            }
//        }).exceptionally(new Function<Throwable, Void>() {
//            @Override
//            public Void apply(Throwable throwable) {
//                System.out.println("throwable:" + throwable);
//                return null;
//            }
//        });
//
//
//        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
//            System.out.println("异步执行");                             //异步执行
//            int i = 1/0;
//            return 1024;
//
//        }).whenComplete(new BiConsumer<Integer, Throwable>() {
//            //回调方法
//
//            @Override
//            public void accept(Integer integer, Throwable throwable) {
//                System.out.println(integer);
//                System.out.println("throwable:" + throwable);
//            }
//        }).exceptionally(new Function<Throwable, Integer>() {
//            //异常处理方法
//
//            @Override
//            public Integer apply(Throwable throwable) {
//                System.out.println("throwable:" + throwable);
//                return 404;
//            }
//        });
//
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


    }


}
