package com.suisrc.jaxrsapi.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * 通过代理拦截器强制连接访问后产生的异常内容
 * 
 * 拦截应该属于一种动态拦截范畴
 * 
 * 与Retry功能相同，不同在于该拦截器值提供一个拦截次数
 * 
 * @author Y13
 *
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface RetryProxy {

    /**
     * 请求重试的次数
     * @return
     */
    int value() default 2;
    
}
