package com.suisrc.jaxrsapi.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.jboss.resteasy.client.jaxrs.ClientBuilderFactory;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.suisrc.core.Global;
import com.suisrc.core.utils.CdiUtils;

/**
 * <p> 程序入口配置抽象
 * 
 * @author Y13
 */
public abstract class AbstractActivator implements ApiActivator {
  protected static final Logger logger = Logger.getLogger(ApiActivator.class.getName());

  /**
   * <p> 调试标记
   */
  protected static final boolean DEBUG = Boolean.getBoolean(JaxrsConsts.DEBUG);

  /**
   * <p> 提供器工厂，序列化和反序列化对象
   */
  protected ResteasyProviderFactory providerFactory = null;

  /**
   * <p> 多线程访问缓冲，可以通知远程访问到线程池
   */
  protected ExecutorService executor = null;

  /**
   * <p> 访问的客户端，每个激活器一个
   */
  protected Client client;

  /**
   * <p> 访问默认代理主机
   */
  protected String proxyHost = null;
  /**
   * <p> 访问默认代理端口
   */
  protected int proxyPort = -1;

  /**
   * <p> 接口实现索引
   */
  private Map<Class<?>, Object> apiImplMap = null;

  /**
   * <p> 构造方法
   */
  protected AbstractActivator() {
    doInitConstruct();
  }

  // ----------------------------------------------------------------ZERO 通用接口属性索引

  /**
   * <p> 构建访问线程池, 默认不使用私有线程池 executor = Executors.newFixedThreadPool(10);
   */
  protected ExecutorService createExecutorService() {
    // RefOne<ExecutorService> ref = new RefOne<>();
    // ScCDI.injectWithNamed(0, null, null, ref::set, Exception::printStackTrace, ScExecutor.class);
    // // ref.get 可能为空，但是为空到时候，会输出注入时候发生到空异常
    // return ref.get();
    return Global.getScExecutor();
  }

  /**
   * <p> 初始化远程访问的客户端
   */
  protected Client createTargetClient(String key) {
    ClientBuilder clientBuilder = createClientBuilder(key);// 配置网络通信内容
    if (clientBuilder instanceof ResteasyClientBuilder) {
      ResteasyClientBuilder rcBuilder = (ResteasyClientBuilder) clientBuilder;
      if (proxyHost != null && proxyPort > 0) {
        // 代理服务器访问
        rcBuilder.defaultProxy(proxyHost, proxyPort);
      }
      if (executor != null) {
        rcBuilder.asyncExecutor(executor); // 配置线程池，默认使用线程池为固定大小最大10个线程
      }
      if (providerFactory != null) {
        rcBuilder.providerFactory(providerFactory);
      }
      // 修正http client，使其达到多线程安全的目的
      ClientBuilderFactory.initHttpEngineThreadSaft(rcBuilder);
    }
    Client client = clientBuilder.build();
    registerTargetClient(key, client);
    return client;
  }

  /**
   * <p> 对访问器注入监听等内容
   * 
   * @param client
   */
  protected void registerTargetClient(String key, Client client) {
    // client.register(new MonitorRequestFilter(this.getClass().getCanonicalName()));
  }

  /**
   * <p> 获取一个Client Builder
   */
  protected ClientBuilder createClientBuilder(String key) {
    // return ClientBuilder.newBuilder();
    return ClientBuilderFactory.newBuilder();
  }

  /**
   * <p> 构造方法调用
   */
  public void doInitConstruct() {}

  /**
   * <p> 构造后被系统调用 进行内容初始化
   */
  @Override
  public void doPostConstruct() {
    proxyHost = createProxyHost();
    proxyPort = createProxyPort();
    // 执行获取执行到线程池
    executor = createExecutorService();
    // 构建客户端创建器
    client = createTargetClient(null);
  }

  /**
   * <p> 获取访问的客户端
   * 
   * @param key
   * @return
   */
  protected Client getClient(String key) {
    return client;
  }

  // ----------------------------------------------------------------ZERO Adapter

  /**
   * <p> 获取系统的对象
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter(String named, Class<T> type) {
    if (type == WebTarget.class) {
      return (T) getClient(named).target(getBaseUrl());
    } else if (type == Client.class) {
      return (T) getClient(named);
    } else if (type == ResteasyProviderFactory.class) {
      return (T) providerFactory;
    }
    return ApiActivator.super.getAdapter(named, type);
  }

  /**
   * <p> 主要是为了防止不支持javaee7.0标准的反向内容注入
   */
  @Override
  public <T> void setAdapter(String named, Class<T> type, T value) {
    if (type == ResteasyProviderFactory.class) {
      providerFactory = (ResteasyProviderFactory) value;
      return;
    }
  }

  // ----------------------------------------------------------------ZERO proxy

  /**
   * 
   * @return
   */
  public String getProxyHost() {
    return proxyHost;
  }

  /**
   * 
   * @return
   */
  public int getProxyPort() {
    return proxyPort;
  }

  /**
   * 
   * @return
   */
  public String createProxyHost() {
    return null;
  }

  /**
   * 
   * @return
   */
  public int createProxyPort() {
    return -1;
  }

  // ----------------------------------------------------------------接口注册

  /**
   * <p> 获取API索引实现内容
   * 
   * @return
   */
  protected Map<Class<?>, Object> getApiImplMap() {
    if (apiImplMap == null) {
      apiImplMap = new HashMap<>();
    }
    return apiImplMap;
  }

  /**
   * <p> 注册接口
   */
  @Override
  public void registerApi(String named, Class<?> api, Object value) {
    if (named == JaxrsConsts.RESTFUL_API_IMPL && value != null && api.isAssignableFrom(value.getClass())) {
      Map<Class<?>, Object> cache = getApiImplMap();
      if (cache != null) {
        cache.put(api, value);
      }
    }
  }

  /**
   * <p> 获取接口实现
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> T getApiImplement(Class<T> apiType) {
    Map<Class<?>, Object> cache = getApiImplMap();
    if (cache != null) {
      return (T) cache.get(apiType);
    }
    return null;
  }

  /**
   * <p> 使用索引+注入的方式实现
   * 
   * @param apiType
   * @return
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  protected <T> T getApiImplement2(Class<T> apiType) {
    Map<Class<?>, Object> cache = getApiImplMap();
    if (cache == null) {
      return null;
    }
    if (cache.isEmpty()) {
      // 集合为空，使用全局索引查询加载
      synchronized (cache) {
        if (cache.isEmpty()) {
          String name = getClass().getCanonicalName() + JaxrsConsts.ACTIVATOR_INDEX_SUFFIX;
          ClassLoader loader = Thread.currentThread().getContextClassLoader();
          try {
            Class indexClass = loader.loadClass(name);
            ApiActivatorIndex indexObject = (ApiActivatorIndex) indexClass.newInstance();
            cache.putAll(indexObject.getApiImpl());
          } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            logger.warning(e.getClass() + ":" + e.getMessage());
          }
        }
      }
    }
    Object value = cache.get(apiType);
    if (value == null) {
      return null;
    }
    if (apiType.isAssignableFrom(value.getClass())) {
      // 该内容被注册过，优先使用注册的内容
      return (T) value;
    }
    if (!(value instanceof Class)) {
      return null;
    }
    Class<?> impClass = (Class) value;
    // 通过注入获取需要调用的内容
    // Named named = impClass.getAnnotation(Named.class);
    // Object obj = named == null ? CdiUtils.select(impClass) : CdiUtils.select(impClass, named);
    return (T) CdiUtils.select(impClass);
  }
}
