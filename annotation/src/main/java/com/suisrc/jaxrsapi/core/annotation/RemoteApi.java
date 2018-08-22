package com.suisrc.jaxrsapi.core.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Restful 提供的注解，用户过滤远程接口还是本地接口使用
 * 
 * 需要指出的是，如果兼容目前已经有的API包，可能出校没有使用RemoteApi标记的情况。 所以目前可用性有待考虑。
 * 
 * @author Y13
 *
 */
@Target({TYPE/* , METHOD, FIELD */ })
@Retention(RUNTIME)
public @interface RemoteApi {
    
    /**
     * 同Path替换类型上的@Path内容
     * @return
     */
    String value() default "";
    
    /**
     * Client关键字, 用于选择生成的客户端方式
     */
    String client() default "";
}
