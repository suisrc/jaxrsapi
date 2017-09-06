package com.suisrc.jaxrsapi.core;

import java.util.Set;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 * 激活接口适配
 * 
 * @author Y13
 *
 */
public interface ApiActivator {

    /**
     * default values container
     */
    final String DVC = Global.getValue(System::getProperty, Consts.KEY_VALUE_PREFIX_DEFAULT, null, 
            Consts.PRE_ENV11T, Consts.PRE_SYSTEM, Consts.PRE_GLOBAL, Consts.PRE_THREAD);

    /**
     * 初始化 initialized
     */
    void init();

    /**
     * 获取基础路径地址
     * 
     * @return
     */
    String getBaseUrl();

    /**
     * 获取接口列表
     * 
     * @return
     */
    Set<Class<?>> getClasses();

    /**
     * 把自己强制转换为其他类型
     */
    @SuppressWarnings("unchecked")
    default <T> T as(Class<T> clazz) {
        if (clazz.isAssignableFrom(getClass())) {
            return (T) this;
        } else {
            throw new RuntimeException(getClass().getCanonicalName() + " not transform to " + clazz.getCanonicalName());
        }
    }

    /**
     * 通过适配器获取数据
     * 
     * @param key
     * @return
     */
    default <T> T getAdapter(String key) {
        if (key == null) {
            return null;
        }
        T value = Global.getValue(key);
        return value != null || DVC == null ? value : Global.getValue(DVC + key);
    }
    
    /**
     * 带有类型的返回
     * @param key
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    default <T> T getAdapter(String key, Class<T> type) {
        if (type == String.class) {
            Object obj = getAdapter(key);
            if (obj != null && type.isAssignableFrom(obj.getClass())) {
                return (T)obj;
            }
        }
        return null;
    }

    /**
     * 设定适配器内容
     * 
     * @param key
     * @param value
     */
    default <T> void setAdapter(String key, T value) {}

    /**
     * 通过适配器获取对象
     * 
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    default <T> T getAdapter(Class<T> type) {
        if (type == WebTarget.class) {
            // 客户端默认新建，为了达到最优访问速度， 最好不使用默认，重写该方法
            return (T) ClientBuilder.newClient().target(getBaseUrl());
        }
        return null;
    }

    /**
     * 设定适配器对象
     * 
     * @param type
     * @param value
     */
    default <T> void setAdapter(Class<T> type, T value) {}

}
