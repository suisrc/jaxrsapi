package com.suisrc.jaxrsapi.client.proxy;

import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.resteasy.client.jaxrs.internal.proxy.ClientInvoker;

/**
 * 拦截器
 * 
 * @author Y13
 *
 */
public interface ClientInvokerInterceptor {
    
    /**
     * 执行操作前执行
     */
    void before(ClientInvoker invoker, ClientInvocation request);
    
    /**
     * 执行操作后执行
     */
    void after(ClientInvoker invoker, ClientResponse response);
    
    /**
     * 发生异常时候执行
     */
    default void exception0(ClientInvoker invoker, Exception e) {}

    /**
     * 执行结束后拦截
     */
    default void finally0(ClientInvoker proxyClientInvoker) {}
}
