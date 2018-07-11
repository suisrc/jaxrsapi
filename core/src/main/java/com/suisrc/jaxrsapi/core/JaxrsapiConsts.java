package com.suisrc.jaxrsapi.core;

import com.suisrc.core.Global;
import com.suisrc.core.ScConsts;

/**
 * 静态变量
 * 
 * @author Y13
 *
 */
public interface JaxrsapiConsts {
    
    /**
     * debug
     */
    final String DEBUG = "com.suisrc.jaxrsapi.debug"; //$NON-NLS-N$

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
    final String PRE_DEFAULT = Global.getValue(System::getProperty, JaxrsapiConsts.KEY_VALUE_PREFIX_DEFAULT, 
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
     * 远程API在系统中运行的模式，如果是单远程模式，只需要使用简单的@Inject即可， 如果同时访问多个远程服务器，需要在使用注入的使用通过@Named进行分离
     * 这里所谓的多个，是同一个restful接口对应多个服务器的情况 如果不愿意使用@Named,可以使用拷贝多份restful接口解决这个问题。这个使用 系统应该运行与单模式
     * 
     * 多模式：同一个restful接口对应多台远程服务器
     */
    final String KEY_REMOTE_API_NULTI_MODE = "com.suisrc.remote-api.runtime.multi-mode"; //$NON-NLS-N$

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
