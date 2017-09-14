package com.suisrc.jaxrsapi.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * value对应的类型必须实现一个proxy方法， 并且第一个参数是String型，用于接受远程访问的url地址
 * 
 * 其他参数为接口参数相同，使用该方法标记的代理不会去访问远程服务器，而是通过本地方法覆盖远程访问
 * 
 * 换句话说，该代理方法的逻辑是存在本地的，而不是远程服务器上。
 * 
 * @author Y13
 *
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface LocalProxy {

    /**
     * 辅助修正参数的内容
     */
    Class<?> value();

    /**
     * Handler 构造的时候是否带有所有者信息
     */
    String master() default "";

    /**
     * 通过类中的静态方法进行获取数据
     * 
     * 该方法必须是一个可以接受一个String参数的静态方法
     */
    String method() default defaultMethod;

    /**
     * 默认的方法名称
     */
    String defaultMethod = "proxy";
}
