package com.suisrc.jaxrsapi.client.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;

import javax.ws.rs.Path;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.ProxyConfig;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.i18n.Messages;
import org.jboss.resteasy.client.jaxrs.internal.proxy.ClientInvoker;
import org.jboss.resteasy.client.jaxrs.internal.proxy.ClientProxy;
import org.jboss.resteasy.client.jaxrs.internal.proxy.MethodInvoker;
import org.jboss.resteasy.client.jaxrs.internal.proxy.ResteasyClientProxy;
import org.jboss.resteasy.client.jaxrs.internal.proxy.SubResourceInvoker;
import org.jboss.resteasy.util.IsHttpMethod;

import com.suisrc.core.Global;

/**
 * <p> 从RESTEasy借用初步工作代码参考
 * 
 * <p> 排除默认构造方法
 * 
 * Ignore default methods
 * 
 * @see org.wildfly.swarm.cdi.jaxrsapi.deployment.ProxyBuilder
 * @see org.jboss.resteasy.client.jaxrs.ProxyBuilder
 * 
 */
public class ProxyBuilder<T> {
  public static final String BUILD_METHOD = "build2";
  public static final String GLOBAL_FILTER_GETTER = "Global_FILTER_GETTER_0103";


  private static final Class<?>[] cClassArgArray = {Class.class};

  private final Class<T> iface;

  private final ResteasyWebTarget webTarget;

  private ClassLoader loader = Thread.currentThread().getContextClassLoader();

  private MediaType serverConsumes;

  private MediaType serverProduces;

  public static <T> ProxyBuilder<T> builder(Class<T> iface, WebTarget webTarget) {
    return new ProxyBuilder<T>(iface, (ResteasyWebTarget) webTarget);
  }

  public static <T> T proxy(final Class<T> iface, WebTarget base, final ProxyConfig config) {
    return proxy(iface, base, config, null);
  }

  @SuppressWarnings("unchecked")
  public static <T> T proxy(final Class<T> iface, WebTarget base, final ProxyConfig config, ClientInvokerFilter filter) {
    if (iface.isAnnotationPresent(Path.class)) {
      Path path = iface.getAnnotation(Path.class);
      if (!path.value().equals("") && !path.value().equals("/")) {
        base = base.path(path.value());
      }
    }
    HashMap<Method, MethodInvoker> methodMap = new HashMap<Method, MethodInvoker>();
    for (Method method : iface.getMethods()) {
      // ignore the as method to allow declaration in client interfaces
      if ("as".equals(method.getName()) && Arrays.equals(method.getParameterTypes(), cClassArgArray)) {
        continue;
      }

      // Ignore default methods
      if (method.isDefault()) {
        continue;
      }

      MethodInvoker invoker;
      Set<String> httpMethods = IsHttpMethod.getHttpMethods(method);
      if ((httpMethods == null || httpMethods.size() == 0) && method.isAnnotationPresent(Path.class)
          && method.getReturnType().isInterface()) {
        invoker = new SubResourceInvoker((ResteasyWebTarget) base, method, config);
      } else {
        invoker = createClientInvoker(iface, method, (ResteasyWebTarget) base, config, filter);
      }
      methodMap.put(method, invoker);
    }

    Class<?>[] intfs = {iface, ResteasyClientProxy.class};

    ClientProxy clientProxy = new ClientProxy(methodMap, base, config);
    // this is done so that equals and hashCode work ok. Adding the proxy to
    // a Collection will cause equals and hashCode to be invoked. The Spring
    // infrastructure had some problems without this.
    clientProxy.setClazz(iface);

    return (T) Proxy.newProxyInstance(config.getLoader(), intfs, clientProxy);
  }

  private static <T> ClientInvoker createClientInvoker(Class<T> clazz, Method method, ResteasyWebTarget base,
      ProxyConfig config, ClientInvokerFilter filter) {

    Set<String> httpMethods = IsHttpMethod.getHttpMethods(method);
    if (httpMethods == null || httpMethods.size() != 1) {
      throw new RuntimeException(Messages.MESSAGES.mustUseExactlyOneHttpMethod(method.toString()));
    }
    // ClientInvoker invoker = new ClientInvoker(base, clazz, method, config);
    // Intercept
    ProxyClientInvoker invoker = new ProxyClientInvoker(base, clazz, method, config);
    invoker.setFilter(filter);
    // set http method
    invoker.setHttpMethod(httpMethods.iterator().next());
    return invoker;
  }

  private ProxyBuilder(Class<T> iface, ResteasyWebTarget webTarget) {
    this.iface = iface;
    this.webTarget = webTarget;
  }

  public ProxyBuilder<T> classloader(ClassLoader cl) {
    this.loader = cl;
    return this;
  }

  public ProxyBuilder<T> defaultProduces(MediaType type) {
    this.serverProduces = type;
    return this;
  }

  public ProxyBuilder<T> defaultConsumes(MediaType type) {
    this.serverConsumes = type;
    return this;
  }

  public ProxyBuilder<T> defaultProduces(String type) {
    this.serverProduces = MediaType.valueOf(type);
    return this;
  }

  public ProxyBuilder<T> defaultConsumes(String type) {
    this.serverConsumes = MediaType.valueOf(type);
    return this;
  }

  public T build() {
    return proxy(iface, webTarget, new ProxyConfig(loader, serverConsumes, serverProduces), null);
  }

  public T buildWithFilter(ClientInvokerFilter filter) {
    return proxy(iface, webTarget, new ProxyConfig(loader, serverConsumes, serverProduces), filter);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public T build2() {
    Function getter = Global.getCacheSafe(Global.getApplicationCache(), GLOBAL_FILTER_GETTER, Function.class);
    if (getter != null) {
      ClientInvokerFilter filter = (ClientInvokerFilter) getter.apply(iface);
      if (filter != null) {
        return buildWithFilter(filter);
      }
    }
    return build();
  }

}
