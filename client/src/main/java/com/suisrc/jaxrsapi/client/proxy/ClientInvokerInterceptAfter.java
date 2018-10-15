package com.suisrc.jaxrsapi.client.proxy;

import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.proxy.ClientInvoker;

/**
 * 拦截器
 * 
 * @author Y13
 *
 */
public interface ClientInvokerInterceptAfter extends ClientInvokerInterceptor {
    
    @Override
    default void before(ClientInvoker invoker, ClientInvocation request) {};
}
