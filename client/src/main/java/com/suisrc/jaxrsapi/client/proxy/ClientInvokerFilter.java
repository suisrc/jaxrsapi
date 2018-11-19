package com.suisrc.jaxrsapi.client.proxy;

import java.util.Map;

import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.resteasy.client.jaxrs.internal.proxy.ClientInvoker;

/**
 * <p> 拦截器
 * 
 * @author Y13
 *
 */
@SuppressWarnings("rawtypes")
public interface ClientInvokerFilter {

  /**
   * <p> 执行操作前执行
   */
  Object before(Map cache, ClientInvoker invoker, ClientInvocation request);

  /**
   * <p> 执行操作后执行
   */
  Object after(Map cache, ClientInvoker invoker, ClientResponse response);

  /**
   * <p> 执行结果
   */
  default Object entity0(Map cache, ClientInvoker invoker, Object result) {
    return null;
  }

  /**
   * <p> 发生异常时候执行
   */
  default Object exception0(Map cache, ClientInvoker invoker, Exception e) {
    return null;
  }

  /**
   * <p> 执行结束后拦截
   */
  default void finally0(Map cache, ClientInvoker invoker) {}
  
  /**
   * <p> 获取缓存
   */
  default Map getCache() {
    return null;
  }
  
}
