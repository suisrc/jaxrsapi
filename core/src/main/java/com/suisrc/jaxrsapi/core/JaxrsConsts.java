package com.suisrc.jaxrsapi.core;

import com.suisrc.core.Global;
import com.suisrc.core.ScConsts;

/**
 * 静态变量
 * 
 * @author Y13
 *
 */
public interface JaxrsConsts {
    
    /**
     * debug
     */
    final String DEBUG = "com.suisrc.jaxrsapi.debug"; //$NON-NLS-N$
    
    /**
     * 接口api
     */
    final String RESTFUL_API_IMPL = "restful-api-impl"; //$NON-NLS-N$

    /**
     * 空字符串
     */
    final String NONE = ""; //$NON-NLS-N$

    // -------------------------value prefix------------------------------//
    
    /**
     * default values container
     * 
     * 默认构造只能在以上内容中
     * 
     * 默认到构造集合
     */
    final String PRE_DEFAULT = Global.getValue(System::getProperty, JaxrsConsts.KEY_VALUE_PREFIX_DEFAULT, 
            null, // 没有默认值
            ScConsts.PRE_GLOBAL, // 全局
            ScConsts.PRE_THREAD, // 线程
            ScConsts.PRE_REQUEST // 请求
        );
    
    /**
     * 环境关键字分隔符
     */
    final char PRE_SPLIT = ':'; //$NON-NLS-N$

    // ----------------------------proxy----------------------------------//
    /**
     * 客户端代理的名字
     */
    final String FIELD_PROXY = "proxy"; //$NON-NLS-N$
    /**
     * 客户端激活器的名字
     */
    final String FIELD_ACTIVATOR = "activator"; //$NON-NLS-N$
    /**
     * 客户端的本身自己的名字
     */
    final String FIELD_THIS = "this"; //$NON-NLS-N$

    /**
     * 注入的时候@Named的间隔符
     */
    final String separator = "/"; //$NON-NLS-N$

    // ------------------------------------------KEY--------------------------------//

    /**
     * 默认的变量容器 该配置内容只能是 E: S: G: T: 其中情况，其他无效
     */
    final String KEY_VALUE_PREFIX_DEFAULT = "com.suisrc.remote-api.values.default-container"; //$NON-NLS-N$

    // ------------------------------------------Value KEY--------------------------------//

    /**
     * 请求内容key,用于线程缓存中获取请求内容
     */
    final String CONTAINER_REQUEST_CONTEXT = "one-container-request-context"; //$NON-NLS-N$
}
