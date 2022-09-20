package com.atguigu.gmall.common.cache;

import java.lang.annotation.*;

/**
 * 元注解：
 * @Target:目标位置
 * TYPE:类
 * FIELD:属性
 * METHOD:方法
 * CONSTRUCTOR:构造器
 *
 * @Retention：存在位置，生命周期
 *
 * java（SOURCE）--class（CLASS）--jvm内存（RUNTIME）
 *
 * @Inherited：是否可以被继承
 *
 * @Documented：是否可以被读取到api
 *
 * @author Aiden
 * @create 2022-09-20 9:36
 */
@Target({ElementType.TYPE,ElementType.METHOD})//目标位置
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface GmallCache {


    String prefix() default "cache";

    String suffix() default "";
}
