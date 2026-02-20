package com.wei.pet.pet_rescue.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 防重复提交自定义注解
 * @author wyr
 */
@Target(ElementType.METHOD) // 作用在方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时有效
public @interface NoRepeatSubmit {

    /**
     * 默认锁定时间：3 秒
     * 意味着同一个用户在 3 秒内，对同一个接口只能请求一次
     */
    long lockTime() default 3;
}
