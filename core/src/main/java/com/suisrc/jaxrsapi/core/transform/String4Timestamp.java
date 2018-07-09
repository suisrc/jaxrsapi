package com.suisrc.jaxrsapi.core.transform;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.suisrc.jaxrsapi.core.iface.ITransformString;

/**
 * Timestamp 转换器
 * @author Y13
 *
 */
public class String4Timestamp implements ITransformString<Timestamp> {

    @Override
    public Timestamp fromString(String str) {
        if (str == null) {
            return null;
        }
        return Timestamp.valueOf(str);
    }

    @Override
    public String toString(Timestamp t) {
        if (t == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(sdf);
    }

}
