package com.suisrc.jaxrsapi.core.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * 在请求出力过程中，非空判断是非常多了
 * 所以这里使用单独的非空判断注解对参数的值进行判断
 * 
 * 该异常优先抛出，并没有进入系统中。
 * 
 * @author Y13
 *
 */
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface NotNull {

    /**
     * 如果反生异常，输出的内容
     */
    String value();
}
