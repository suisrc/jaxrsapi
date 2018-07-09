package com.suisrc.jaxrsapi.core.transform;

import com.suisrc.jaxrsapi.core.iface.ITransformString;

/**
 * Double转换器
 * @author Y13
 *
 */
public class String4Double implements ITransformString<Double> {

    @Override
    public Double fromString(String str) {
        if (str == null) {
            return null;
        }
        return Double.valueOf(str);
    }

    @Override
    public String toString(Double t) {
        if (t == null) {
            return null;
        }
        return String.valueOf(t);
    }

}
