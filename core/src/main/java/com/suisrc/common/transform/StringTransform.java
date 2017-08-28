package com.suisrc.common.transform;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import com.suisrc.common.iface.ITransformString;

/**
 * 类型转换
 * 
 * @author Y13
 *
 */
public final class StringTransform {

    /**
     * 创建翻译器类型 boolean byte char short int long float double
     * 
     * @param type
     * @return
     */
    public static <T> ITransformString<T> createTransform(Class<T> type) {
        ITransformString<T> transform = createPrimitiveTransform(type); // 基本数据类型
        if (transform != null) {
            return transform;
        }
        transform = createTimeTransform(type); // 时间数据类型
        if (transform != null) {
            return transform;
        }
        throw new RuntimeException("无法找到默认提供的类型转换规则, 请确认：" + type.getCanonicalName());
    }

    /**
     * 创建翻译器类型 boolean byte char short int long float double
     * 
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T> ITransformString<T> createPrimitiveTransform(Class<T> type) {
        if (type == String.class) {
            return (ITransformString<T>) new String4String(); // 防止传入错误的内容而反生意外
        }
        if (type == boolean.class || type == Boolean.class) {
            return (ITransformString<T>) new String4Boolean();
        }
        if (type == byte.class || type == Byte.class) {
            return (ITransformString<T>) new String4Byte();
        }
        if (type == char.class || type == Character.class) {
            return (ITransformString<T>) new String4Character();
        }
        if (type == short.class || type == Short.class) {
            return (ITransformString<T>) new String4Short();
        }
        if (type == int.class || type == Integer.class) {
            return (ITransformString<T>) new String4Integer();
        }
        if (type == long.class || type == Long.class) {
            return (ITransformString<T>) new String4Long();
        }
        if (type == float.class || type == Float.class) {
            return (ITransformString<T>) new String4Long();
        }
        if (type == double.class || type == Double.class) {
            return (ITransformString<T>) new String4Double();
        }
        return null; // 最后没有知道何时的
    }

    /**
     * 创建翻译器类型 Date Time Timestamp
     * 
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T> ITransformString<T> createTimeTransform(Class<T> type) {
        if (type == Date.class) {
            return (ITransformString<T>) new String4Date();
        }
        if (type == Time.class) {
            return (ITransformString<T>) new String4Time();
        }
        if (type == Timestamp.class) {
            return (ITransformString<T>) new String4Timestamp();
        }
        return null;
    }

}
