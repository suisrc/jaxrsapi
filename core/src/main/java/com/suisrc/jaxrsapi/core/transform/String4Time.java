package com.suisrc.jaxrsapi.core.transform;

import java.sql.Time;

import com.suisrc.jaxrsapi.core.iface.ITransformString;

/**
 * Time转换器
 * @author Y13
 *
 */
public class String4Time implements ITransformString<Time> {

    @Override
    public Time fromString(String str) {
        if (str == null) {
            return null;
        }
        return Time.valueOf(str);
    }

    @Override
    public String toString(Time t) {
        if (t == null) {
            return null;
        }
        return t.toString();
    }

}
