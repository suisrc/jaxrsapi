package com.suisrc.common.transform;

import com.suisrc.common.iface.ITransformString;

/**
 * Boolean转换器
 * 
 * @author Y13
 *
 */
public class String4Boolean implements ITransformString<Boolean> {

    @Override
    public Boolean fromString(String str) {
        if (str == null) {
            return false;
        }
        return Boolean.valueOf(str);
    }

    @Override
    public String toString(Boolean t) {
        if (t == null) {
            return null;
        }
        return String.valueOf(t);
    }

}
