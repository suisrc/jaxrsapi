package com.suisrc.jaxrsapi.core;

import java.util.Set;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import com.suisrc.core.Global;

/**
 * 激活接口适配
 * 
 * @author Y13
 *
 */
public interface ApiActivator {

    /**
     * 构造方法执行过程中到初始化
     */
    default void initConstruct() {};
    
    /**
     * 构造方法结束后到初始化
     */
    default void postConstruct() {};

    /**
     * 获取基础路径地址
     * 
     * @return
     */
    String getBaseUrl();

    /**
     * 获取接口列表
     */
    Set<Class<?>> getClasses();
    
    /**
     * 是否为标准Inject接口
     * 如果是标准Jboss接口，注入时候，会生成@Inject,否则在正常构造方法时候，会执行注入调用
     */
    default boolean isStdInject() {
        return false;
    }

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
        return getAdapter(key, (Class<T>) null);
    }
    
    /**
     * 带有类型的返回
     * @param key
     * @param type
     * @return
     */
    default <T> T getAdapter(String key, Class<T> type) {
        if (key == null) {
            return null;
        }
        T value = null;
        if (key.indexOf(Consts.PRE_SPLIT) < 0) {
            // 使用默认变量环境
            if (Consts.PRE_DEFAULT != null) {
                value = Global.getValue(Consts.PRE_DEFAULT + key, type);
            }
        } else {
            // 直接查询
            value = Global.getValue(key, type);
        }
        return value;
    }

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
     * 设定适配器内容
     * 
     * @param key
     * @param value
     */
    default <T> void setAdapter(String key, T value) {}

    /**
     * 设定适配器对象
     * 
     * @param type
     * @param value
     */
    default <T> void setAdapter(Class<T> type, T value) {}

}
