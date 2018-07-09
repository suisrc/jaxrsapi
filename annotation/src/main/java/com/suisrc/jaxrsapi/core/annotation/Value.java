package com.suisrc.jaxrsapi.core.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 获取服务器系统中的数据
 * 
 * 目前获取的途径有
 * 
 * 系统环境变量E:
 * 系统属性变量S:
 * 线程环境变量T:
 * 请求环境变量R:
 * 回话环境变量SE:
 * 全局环境变量G:
 * 集群环境变量SW:
 * 
 * 以上四种方式获取
 * 
 * 获取途径是通过调用API接口的激活器中的getAdapter接口实现的，所以可用通过重写该接口，对所控制的值进行自定义控制
 * 
 * @author Y13
 *
 */
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
public @interface Value {

    /**
     * 获取服务器中的数据作为默认参数
     * 
     * 如果使用 E:, S:, G:, T:, R:, SE:, SW:表示可以从系统不同的位置获取该变量
     * 
     * 当然也可以使用不带前缀的变量名称
     */
    String value();
}
