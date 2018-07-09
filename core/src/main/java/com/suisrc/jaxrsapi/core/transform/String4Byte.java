package com.suisrc.jaxrsapi.core.transform;

import com.suisrc.jaxrsapi.core.iface.ITransformString;

/**
 * Byte转换器
 * 
 * @author Y13
 *
 */
public class String4Byte implements ITransformString<Byte> {

    @Override
    public Byte fromString(String str) {
        if (str == null) {
            return null;
        }
        return Byte.valueOf(str);
    }

    @Override
    public String toString(Byte t) {
        if (t == null) {
            return null;
        }
        return String.valueOf(t);
    }

}
