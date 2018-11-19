package com.suisrc.jaxrsapi.client.proxy;

import java.lang.reflect.Method;
import java.util.Map;

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

  private ClientInvokerFilter filter = null;

  public ProxyClientInvoker(ResteasyWebTarget parent, Class<?> declaring, Method method, ProxyConfig config) {
    super(parent, declaring, method, config);
  }

  public void setFilter(ClientInvokerFilter filter) {
    this.filter = filter;
  }

  public Object invoke(Object[] args) {
    if (filter == null) {
      return super.invoke(args);
    }
    Map<?, ?> cache = filter.getCache();
    try {
      ClientInvocation request = createRequest(args);
      Object res0 = filter.before(cache, this, request);
      if (res0 != null) { return res0; }
      
      ClientResponse response = (ClientResponse) request.invoke();
      res0 = filter.after(cache, this, response);
      if (res0 != null) { return res0; }
      
      ClientContext context = new ClientContext(request, response, entityExtractorFactory);
      Object result = extractor.extractEntity(context);
      res0 = filter.entity0(cache, this, result);
      if (res0 != null) { return res0; }
      
      return result;
    } catch (Exception e) {
      Object res0 = filter.exception0(cache, this, e);
      if (res0 != null) { return res0; }
      throw e;
    } finally {
      filter.finally0(cache, this);
    }
  }
}
