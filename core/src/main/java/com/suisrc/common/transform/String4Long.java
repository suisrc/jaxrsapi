package com.suisrc.common.transform;

import com.suisrc.common.iface.ITransformString;

/**
 * Long转换器
 * @author Y13
 *
 */
public class String4Long implements ITransformString<Long> {

    @Override
    public Long fromString(String str) {
        if (str == null) {
            return null;
        }
        if(str.indexOf('.') >= 0) {
            return Double.valueOf(str).longValue();
        } else {
            return Long.valueOf(str);
        }
    }

    @Override
    public String toString(Long t) {
        if (t == null) {
            return null;
        }
        return String.valueOf(t);
    }


}
