package com.suisrc.common.iface;

import com.suisrc.jaxrsapi.core.runtime.ITransform4Str;

/**
 * 数据类型转换 目前数据转化的时候 encrypt/decrypt
 * 
 * @author Y13
 *
 */
public interface ITransform<T, F> extends ITransform4Str<T> {

    /**
     * 把数据类型转换为内存中数据类型
     * 
     * @param u
     * @return
     */
    T fromString(String F);

    /**
     * 把内存中数据类型转换为数据类型
     * 
     * @param t
     * @return
     */
    F toString(T t);
}
