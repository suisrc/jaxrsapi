package com.suisrc.jaxrsapi.core.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.suisrc.jaxrsapi.core.runtime.ITransform4Str;

/**
 * 配合javax.ws.rs.DefaultValue注解使用，转换其内容
 * 
 * 如果没有javax.ws.rs.DefaultValue,该注解没有任何作用
 * 
 * @author Y13
 *
 */
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
public @interface TfDefaultValue {

    /**
     * 该方法获取类型转换器
     */
    Class<? extends ITransform4Str<?>> value();
}
