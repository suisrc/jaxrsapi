package com.suisrc.jaxrsapi.client.proxy;

import java.util.Map;

import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.resteasy.client.jaxrs.internal.proxy.ClientInvoker;

/**
 * 拦截器
 * 
 * @author Y13
 *
 */
@SuppressWarnings("rawtypes")
public interface ClientInvokerFilterBefore extends ClientInvokerFilter {
    
    @Override
    default Object after(Map cache, ClientInvoker invoker, ClientResponse response) {
      return null;
    };
}
