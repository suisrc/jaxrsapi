package com.suisrc.jaxrsapi.core.transform;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.suisrc.jaxrsapi.core.exception.TransformException;
import com.suisrc.jaxrsapi.core.iface.ITransformString;

/**
 * Date转换器
 * @author Y13
 *
 */
public class String4Date implements ITransformString<Date> {

    @Override
    public Date fromString(String str) {
        if (str == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(str);
        } catch (ParseException e) {
            throw TransformException.create(e);
        }
    }

    @Override
    public String toString(Date t) {
        if (t == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(t);
    }

}
