package com.suisrc.jaxrsapi.core.transform;

import com.suisrc.jaxrsapi.core.iface.ITransformString;

/**
 * String转换器
 * @author Y13
 *
 */
public class String4String implements ITransformString<String> {

    @Override
    public String fromString(String str) {
        return str;
    }

    @Override
    public String toString(String str) {
        return str;
    }

}
