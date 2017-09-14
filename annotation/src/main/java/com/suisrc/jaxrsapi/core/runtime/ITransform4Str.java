package com.suisrc.jaxrsapi.core.runtime;

/**
 * DefaultValue注解用的转换器接口
 * @author Y13
 *
 * @param <T>
 */
public interface ITransform4Str<T>  {
    
    /**
     * 数据类型转换
     */
    T fromString(String str);

}
