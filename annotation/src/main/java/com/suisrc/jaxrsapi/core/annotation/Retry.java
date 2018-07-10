package com.suisrc.jaxrsapi.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.suisrc.jaxrsapi.core.runtime.RetryPredicate;

/**
 * 如果发生异常等或者内容不对，请求重试
 * 
 * 主要注意，在使用该内容时候，异常会被屏蔽，如果需要异常，
 * 需要在最后一次重试验证时候，对异常记性判断
 * 
 * 重试注解请不要和LocalProxy复合使用，LocalProxy表示本地代理请求，重试多次没有任何意义。
 * 
 * @author Y13
 *
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Retry {

    /**
     * RetryPredicate 构造的时候是否带有所有者信息
     */
    String master() default "";

    /**
     * 请求重试的次数
     * @return
     */
    int count() default 2;
    
    /**
     * 断言请求重试
     * @return
     */
    Class<? extends RetryPredicate<?>> value();
}
