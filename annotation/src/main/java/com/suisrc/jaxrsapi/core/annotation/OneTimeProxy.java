package com.suisrc.jaxrsapi.core.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 该注解标记远程访问接口是一次可用性，实现中不包含一个可用的proxy代理，当发生访问请求的时候，
 * 生成远程访问需要的内容。
 * @author Y13
 *
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface OneTimeProxy {
}
