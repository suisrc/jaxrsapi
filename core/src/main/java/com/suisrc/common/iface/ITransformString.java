package com.suisrc.common.iface;

/**
 * 数据类型转换 目前数据转化的时候，只支持String类型 encrypt/decrypt
 * 
 * @author Y13
 *
 */
public interface ITransformString<T> extends ITransform<T, String> {

    /**
     * 把数据类型转换为内存中数据类型
     * 
     * @param u
     * @return
     */
    T fromString(String str);

    /**
     * 把内存中数据类型转换为数据类型
     * 
     * @param t
     * @return
     */
    String toString(T t);
}
