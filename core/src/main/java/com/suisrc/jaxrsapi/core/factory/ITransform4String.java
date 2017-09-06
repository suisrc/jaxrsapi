package com.suisrc.jaxrsapi.core.factory;

import com.suisrc.common.exception.TransformException;
import com.suisrc.common.iface.ITransformString;

/**
 * DefaultValue注解用的转换器接口
 * @author Y13
 *
 * @param <T>
 */
public interface ITransform4String<T> extends ITransformString<T> {
    
    /**
     * 屏蔽该方法，无效，只需要fromString
     */
    default String toString(T t) {
        throw new TransformException("转换不支持");
    }

}
