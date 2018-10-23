package com.suisrc.jaxrsapi.core.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.suisrc.jaxrsapi.core.runtime.ReviseHandler;

/**
 * 对参数进行拦截修正 当注解在parameter和field上的时候，修正是对参数有效的， 当注解在method上的时候，修正是对结果集有效的
 * 
 * @author Y13
 *
 */
@Target({PARAMETER, FIELD, METHOD})
@Retention(RUNTIME)
public @interface Reviser {

  /**
   * 辅助修正参数的内容
   */
  Class<? extends ReviseHandler<?>> value();

  /**
   * ReviseHandler 构造的时候是否带有所有者信息
   */
  String master() default "";

}
