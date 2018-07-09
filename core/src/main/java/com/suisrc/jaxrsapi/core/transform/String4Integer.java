package com.suisrc.jaxrsapi.core.transform;

import com.suisrc.jaxrsapi.core.iface.ITransformString;

/**
 * Integer转换器
 * @author Y13
 *
 */
public class String4Integer implements ITransformString<Integer> {

    @Override
    public Integer fromString(String str) {
        if (str == null) {
            return null;
        }
        if(str.indexOf('.') >= 0) {
            return Double.valueOf(str).intValue();
        } else {
            return Integer.valueOf(str);
        }
    }

    @Override
    public String toString(Integer t) {
        if (t == null) {
            return null;
        }
        return String.valueOf(t);
    }

}
