package com.suisrc.jaxrsapi.core.transform;

import com.suisrc.jaxrsapi.core.iface.ITransformString;

/**
 * short转换器
 * @author Y13
 *
 */
public class String4Short implements ITransformString<Short> {

    @Override
    public Short fromString(String str) {
        if (str == null) {
            return null;
        }
        if(str.indexOf('.') >= 0) {
            return Double.valueOf(str).shortValue();
        } else {
            return Short.valueOf(str);
        }
    }

    @Override
    public String toString(Short t) {
        if (t == null) {
            return null;
        }
        return String.valueOf(t);
    }

}
