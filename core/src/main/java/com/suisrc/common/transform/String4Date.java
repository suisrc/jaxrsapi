package com.suisrc.common.transform;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.suisrc.common.exception.TransformException;
import com.suisrc.common.iface.ITransformString;

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
