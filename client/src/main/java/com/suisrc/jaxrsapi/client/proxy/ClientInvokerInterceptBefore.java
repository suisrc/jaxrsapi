package com.suisrc.jaxrsapi.client.proxy;

import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.resteasy.client.jaxrs.internal.proxy.ClientInvoker;

/**
 * 拦截器
 * 
 * @author Y13
 *
 */
public interface ClientInvokerInterceptBefore extends ClientInvokerInterceptor {
    
    @Override
    default void after(ClientInvoker invoker, ClientResponse response) {};
}
