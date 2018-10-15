package com.suisrc.jaxrsapi.soap.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Y13
 *
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Soap12XmlMethod {
    
    String namespace() default "";

    String operationName() default "";
    
    /**
     * 默认为operationName + Response
     * @return
     */
    String operationResponse() default "";
}
