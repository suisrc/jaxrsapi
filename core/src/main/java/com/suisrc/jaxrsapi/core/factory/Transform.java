package com.suisrc.jaxrsapi.core.factory;

import com.suisrc.common.exception.TransformException;
import com.suisrc.common.iface.ITransformString;
import com.suisrc.common.transform.StringTransform;
import com.suisrc.jaxrsapi.core.runtime.ITransform4Str;

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
    @SuppressWarnings("unchecked")
    public static <T> T tf(Class<T> type, String value) {
        if (type == String.class) {
            return (T) value; // String类型，不用转换
        }
        if (ITransform4Str.class.isAssignableFrom(type)) {
            try {
                return (T) ((ITransform4Str<?>)type.newInstance()).fromString(value);
            } catch (Exception e) {
                throw new TransformException(e);
            }
        }
        if (value.isEmpty() || value.toLowerCase().equals("null")) {
            return null;
        }
        ITransformString<T> ts = StringTransform.createTransform(type);
        return ts.fromString(value);
    }
}
