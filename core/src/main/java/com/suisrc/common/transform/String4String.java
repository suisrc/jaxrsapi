package com.suisrc.common.transform;

import com.suisrc.common.iface.ITransformString;

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
