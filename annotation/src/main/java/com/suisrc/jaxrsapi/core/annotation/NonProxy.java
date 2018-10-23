package com.suisrc.jaxrsapi.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 执行的方法是不需要构建代理的
 * 
 * 常用于标记不需要执行代理和集成的方法
 * 
 * 通常情况下请求的方法必须通过GET, POST, PUT, DELETE进行标记， 但是如果有这些标记，而且不想实现代理内容，可以通过该字段标记进行代理请求的忽略
 * 
 * 也可以用于禁用和屏蔽远程访问。
 * 
 * @author Y13
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface NonProxy {

  /**
   * 在实际编程的时候，很多时候，禁用是后期更改的，但是会遇到无法修改代码的问题 所以在这里， 我们通过参数，获取该Proxy是否有效，当然这个就是需要远程访问 代码的运行时构建的特性。
   * 
   * 远程访问代码生成的时候，会通过该字段的value作为key想系统变量中查找对应的值 如果值为true，表示禁用该远程接口。 默认情况下，如果value为""表示该接口永久禁用
   * 当value不为空的时候，该接口是可用的，只有到System.getProperty获取的值为true 的时候，该接口才被禁用
   * 
   * 该注解可以在本地框架中禁用远程访问的API接口。
   */
  String value() default "";
}
