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
    default void doInitConstruct() {};
    
    /**
     * 构造方法结束后到初始化
     */
    default void doPostConstruct() {};

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
     * 带有类型的返回
     * @param named
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    default <T> T getAdapter(String named, Class<T> type) {
        if (type == WebTarget.class) {
            // 客户端默认新建，为了达到最优访问速度， 最好不使用默认，重写该方法
            return (T) ClientBuilder.newClient().target(getBaseUrl());
        }
        if (named == null) {
            return null;
        }
        T value = null;
        if (named.length() > 2 && named.indexOf('{') >= 0) {
            // 带有附加变量的查询方式
            StringBuilder keysbir = new StringBuilder();
            int offset = 0;
            int start = named.indexOf('{');
            while (start >= offset) {
                if (offset < start) {
                    keysbir.append(named.substring(offset, start));
                }
                int end = named.indexOf('}', start);
                if (end < 0) {
                    return null; // 无法处理
                }
                String keykey = named.substring(start + 1, end);
                if (keykey.isEmpty()) {
                    return null; // 无法处理
                }
                // 警告：这里有递归，本身递归就要危险性
                String keyvalue = getAdapter(keykey, String.class);
                if (keyvalue == null) {
                    return null; // 无法处理
                }
                keysbir.append(keyvalue);
                offset = end + 1;
                start = named.indexOf('{', offset);
            }
            if (offset < named.length() - 1) {
                // 把结尾的内容增加进去
                keysbir.append(named.substring(offset));
            }
            value = getAdapter(keysbir.toString(), type);
        } else if (named.indexOf(JaxrsapiConsts.PRE_SPLIT) < 0) {
            // 使用默认变量环境
            if (JaxrsapiConsts.PRE_DEFAULT != null) {
                value = Global.getValue(JaxrsapiConsts.PRE_DEFAULT + named, type);
            }
        } else {
            // 直接查询
            value = Global.getValue(named, type);
        }
        return value;
    }

    /**
     * 设定适配器内容
     * 
     * @param key
     * @param value
     */
    default <T> void setAdapter(String named, Class<T> type, T value) {}

}
