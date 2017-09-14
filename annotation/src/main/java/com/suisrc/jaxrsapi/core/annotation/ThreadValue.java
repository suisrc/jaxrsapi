package com.suisrc.jaxrsapi.core.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 获取服务器系统中的数据
 * 
 * 该注解只能通过静态进行获取线程变量
 * 
 * 需要指定访问的静态类和访问方法
 * 
 * 返回值的类型需要自己控制，在系统中不会检测
 * 
 * @author Y13
 *
 */
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
public @interface ThreadValue {

    /**
     * 检索key
     * 
     * @return
     */
    String value();

    /**
     * 通过线程上获取数据需要通过的静态class
     * 
     * 默认使用Global类进行获取, 在解析器中定义
     */
    Class<?> clazz() default Void.class;//Global.class;

    /**
     * 通过类中的静态方法进行获取数据
     * 
     * 该方法必须是一个可以接受一个String参数的静态方法
     */
    String method() default defaultMethod;

    /**
     * 默认的方法名称
     */
    String defaultMethod = "getThreadCache";
}
