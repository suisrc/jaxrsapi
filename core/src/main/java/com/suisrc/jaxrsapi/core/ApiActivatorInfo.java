package com.suisrc.jaxrsapi.core;

import java.util.Set;

/**
 * 截获器接口信息内容
 * 
 * 主要用户代码生成
 * @author Y13
 *
 */
public interface ApiActivatorInfo {

    /**
     * 通过适配器接口获取信息
     * 
     * @param key
     * @param type
     * @return
     */
    <T> T getAdapter(String key, Class<T> type);

    /**
     * 是否为多接口模式
     * @return
     */
    boolean isMulitMode();

    /**
     * 是否为CDI标准注入
     * @return
     */
    boolean isStdInject();

    /**
     * 获取激活器名称
     * @return
     */
    String getActivatorName();

    /**
     * 获取所有的接口内容
     * @return
     */
    Set<Class<?>> getClasses();

    /**
     * 获取激活器类名
     * @return
     */
    String getActivatorClassName();

    /**
     * 获取几乎器包名
     */
    String getActivatorPackageName();

}
