package com.suisrc.jaxrsapi.core;

/**
 * 扩展接口以创建基于CDI的JAXRS客户端。
 * 
 * @author Y13
 * @see org.wildfly.swarm.client.jaxrs.ServiceClient
 * @param <T>
 */
public interface ServiceClient {
    /**
     * 一些默认默认的方法名字
     */
    public static final String MED_initialize = "init";
    public static final String MED_setActivator = "setActivator";
    public static final String MED_getActivator = "getActivator";
    public static final String MED_getAdapter = "getAdapter";

    /**
     * 获取激活器,激活器存放远程服务器信息 每一个客户端都都有一个归属的激活器
     * 
     * @return
     */
    ApiActivator getActivator();

    // ------------------------------------初始化-------------------------------------//
    /**
     * initialized
     */
    default void init() {}

    // ------------------------------------配置信息------------------------------------//

    /**
     * 重置激活器
     * 
     * @param activator
     */
    default void setActivator(ApiActivator activator) {}

    /**
     * 通过适配器获取数据
     * 
     * @param key
     * @return
     */
    default <T> T getAdapter(String key) {
        return getActivator().getAdapter(key);
    }

    /**
     * 通过适配器获取数据
     * 
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    default <T> T getAdapter(Class<T> type) {
        return type == ApiActivator.class ? (T) getActivator() : getActivator().getAdapter(type);
    }

}
