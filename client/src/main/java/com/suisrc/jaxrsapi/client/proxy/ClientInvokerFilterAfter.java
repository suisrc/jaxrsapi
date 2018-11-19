package com.suisrc.jaxrsapi.client.proxy;

import java.util.Map;

import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.proxy.ClientInvoker;

/**
 * 拦截器
 * 
 * @author Y13
 *
 */
@SuppressWarnings("rawtypes")
public interface ClientInvokerFilterAfter extends ClientInvokerFilter {
    
    @Override
    default Object before(Map cache, ClientInvoker invoker, ClientInvocation request) {
      return null;
    };
}
