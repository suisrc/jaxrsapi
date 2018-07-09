package com.suisrc.jaxrsapi.core.transform;

import com.suisrc.jaxrsapi.core.iface.ITransformString;

/**
 * Character 转换器
 * 
 * @author Y13
 *
 */
public class String4Character implements ITransformString<Character> {

    @Override
    public Character fromString(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        return str.charAt(0);
    }

    @Override
    public String toString(Character t) {
        if (t == null) {
            return null;
        }
        return String.valueOf(t);
    }

}
