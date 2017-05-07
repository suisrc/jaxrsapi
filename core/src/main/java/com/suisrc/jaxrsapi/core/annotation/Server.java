package com.suisrc.jaxrsapi.core.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 服务器基本信息配置
 * 
 * 该字段暂时没有使用
 * @author Y13
 *
 */
@Target({ TYPE/*, METHOD, FIELD*/ })
@Retention(RUNTIME)
@Deprecated
public @interface Server {

	/**
	 * 服务器基本路径地址
	 * ${xxxx.yyyy}表示路径是获取系统环境变量中的地址，否则直接使用地址
	 * @return
	 */
	String baseUrl();
	
	/**
	 * @ApplicationPath中的内容
	 * @return
	 */
	String applicationPath() default "";
}
