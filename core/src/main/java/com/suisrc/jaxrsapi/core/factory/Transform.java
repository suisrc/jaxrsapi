package com.suisrc.jaxrsapi.core.factory;

import com.suisrc.jaxrsapi.core.exception.TransformException;
import com.suisrc.jaxrsapi.core.iface.ITransformString;
import com.suisrc.jaxrsapi.core.runtime.ITransform4Str;
import com.suisrc.jaxrsapi.core.transform.StringTransform;

/**
 * 类型转换
 * 
 * @author Y13
 */
public class Transform {

    /**
     * 转换的方法
     * transform
     */
    static final String METHOD = "tf";

    /**
     * 数据转换
     * 
     * @param type 返回的类型
     * @param value 数值
     * @return
     */
    public static Object tf(Class<?> type, String value) {
        if (type == String.class) {
            return value; // String类型，不用转换
        }
        if (ITransform4Str.class.isAssignableFrom(type)) {
            try {
                return ((ITransform4Str<?>)type.newInstance()).fromString(value);
            } catch (Exception e) {
                throw new TransformException(e);
            }
        }
        if (value.isEmpty() || value.equalsIgnoreCase("null")) {
            return null;
        }
        ITransformString<?> ts = StringTransform.createTransform(type);
        return ts.fromString(value);
    }
    
}
