package com.suisrc.jaxrsapi.client.proxy;

import java.lang.reflect.Method;

import org.jboss.resteasy.client.jaxrs.ProxyConfig;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.resteasy.client.jaxrs.internal.proxy.ClientInvoker;
import org.jboss.resteasy.client.jaxrs.internal.proxy.extractors.ClientContext;

/**
 * 
 * @author Y13
 *
 */
public class ProxyClientInvoker extends ClientInvoker {
    
    private ClientInvokerInterceptor interceptor = null;

    public ProxyClientInvoker(ResteasyWebTarget parent, Class<?> declaring, Method method, ProxyConfig config) {
        super(parent, declaring, method, config);
    }
    
    public void setInterceptor(ClientInvokerInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public Object invoke(Object[] args) {
        if (interceptor == null) {
            return super.invoke(args);
        }
        try {
            ClientInvocation request = createRequest(args);
            // 拦截器
            interceptor.before(this, request);
            ClientResponse response = (ClientResponse) request.invoke();
            // 拦截器
            interceptor.after(this, response);
            ClientContext context = new ClientContext(request, response, entityExtractorFactory);
            return extractor.extractEntity(context);
        } catch (Exception e) {
            // 拦截器
            interceptor.exception0(this, e);
            throw e;
        } finally {
            // 拦截器
            interceptor.finally0(this);
        }
    }
}
