package org.jboss.resteasy.client.jaxrs;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;

import com.suisrc.core.utils.ReflectionUtils;

/**
 * 远程访问执行代理 确保访问进行时候的线程安全 用于取代ApacheHttpClient4Engine
 * 
 * @author Y13
 *
 */
@SuppressWarnings("deprecation")
public class ClientHttpEngineProxy4 extends ApacheHttpClient4Engine implements ClientHttpEngine {

  /**
   * 解决多线程问题 每一个线程上都会有一个http客户端引擎
   */
  private ThreadLocal<ApacheHttpClient4Engine> local = new ThreadLocal<>();

  private HttpParams params;

  private SchemeRegistry registry;

  protected HttpHost proxy;

  protected int responseBufferSize;

  protected HostnameVerifier hostnameVerifier;

  protected SSLContext sslContext;

  protected int connectionPoolSize;

  protected TimeUnit connectionTTLUnit;

  protected long connectionTTL;

  protected int maxPooledPerRoute;

  public ClientHttpEngineProxy4 setRequestConfigBuilderSafe(HttpParams params) {
    this.params = params;
    return this;
  }

  public ClientHttpEngineProxy4 setRegistrySafe(SchemeRegistry registry) {
    this.registry = registry;
    return this;
  }

  public ClientHttpEngineProxy4 setProxySafe(HttpHost proxy) {
    this.proxy = proxy;
    return this;
  }

  public ClientHttpEngineProxy4 setResponseBufferSizeSafe(int responseBufferSize) {
    this.responseBufferSize = responseBufferSize;
    return this;
  }

  public ClientHttpEngineProxy4 setHostnameVerifierSafe(HostnameVerifier hostnameVerifier) {
    this.hostnameVerifier = hostnameVerifier;
    return this;
  }

  public ClientHttpEngineProxy4 setSslContextSafe(SSLContext sslContext) {
    this.sslContext = sslContext;
    return this;
  }

  public ClientHttpEngineProxy4 setConnectionPoolSizeSafe(int connectionPoolSize) {
    this.connectionPoolSize = connectionPoolSize;
    return this;
  }

  public ClientHttpEngineProxy4 setConnectionTTLUnitSafe(TimeUnit connectionTTLUnit) {
    this.connectionTTLUnit = connectionTTLUnit;
    return this;
  }

  public ClientHttpEngineProxy4 setConnectionTTLSafe(long connectionTTL) {
    this.connectionTTL = connectionTTL;
    return this;
  }

  public ClientHttpEngineProxy4 setMaxPooledPerRouteSafe(int maxPooledPerRoute) {
    this.maxPooledPerRoute = maxPooledPerRoute;
    return this;
  }

  protected ApacheHttpClient4Engine get() {
    ApacheHttpClient4Engine engine = local.get();
    if (engine == null) {
      engine = createClientHttpEngine();
      local.set(engine);
    }
    return engine;
  }

  protected ApacheHttpClient4Engine createClientHttpEngine() {
    // 构建http访问客户端
    DefaultHttpClient httpClient = new DefaultHttpClient(createHttpClientConnectionManager(), params);
    // 构建http访问引擎
    ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(httpClient, true);
    engine.setResponseBufferSize(responseBufferSize);
    engine.setHostnameVerifier(hostnameVerifier);
    // this may be null. We can't really support this with Apache Client.
    engine.setSslContext(sslContext);
    if (proxy != null) {
      // engine.setDefaultProxy(proxy);
      ReflectionUtils.invoke(ApacheHttpClient4Engine.class, engine, "setDefaultProxy", new Class<?>[] {HttpHost.class},
          new Object[] {proxy});
    }
    return engine;
  }

  protected ClientConnectionManager createHttpClientConnectionManager() {
    if (connectionPoolSize > 0) {
      PoolingClientConnectionManager tcm = new PoolingClientConnectionManager(registry, connectionTTL, connectionTTLUnit);
      tcm.setMaxTotal(connectionPoolSize);
      tcm.setDefaultMaxPerRoute(maxPooledPerRoute);
      return tcm;
    } else {
      return new BasicClientConnectionManager(registry);
    }
  }

  // -------------------------------------ClientHttpEngine--------------------------------//

  @Override
  public SSLContext getSslContext() {
    return get().getSslContext();
  }

  @Override
  public HostnameVerifier getHostnameVerifier() {
    return get().getHostnameVerifier();
  }

  @Override
  public ClientResponse invoke(ClientInvocation request) {
    return get().invoke(request);
  }

  @Override
  public void close() {
    get().close();
  }

  // -------------------------------------ApacheHttpClient4Engine--------------------------------//

  // @Override
  // public SSLContext getSslContext() {
  // return get().getSslContext();
  // }

  // @Override
  // public HostnameVerifier getHostnameVerifier() {
  // return get().getHostnameVerifier();
  // }

  // @Override
  // public ClientResponse invoke(ClientInvocation request) {
  // return get().invoke(request);
  // }

  // @Override
  // public void close() {
  // get().close();
  // }

  @Override
  public int getResponseBufferSize() {
    return get().getResponseBufferSize();
  }

  @Override
  public void setResponseBufferSize(int responseBufferSize) {
    get().setResponseBufferSize(responseBufferSize);
  }

  @Override
  public int getFileUploadInMemoryThresholdLimit() {
    return get().getFileUploadInMemoryThresholdLimit();
  }

  @Override
  public void setFileUploadInMemoryThresholdLimit(int fileUploadInMemoryThresholdLimit) {
    get().setFileUploadInMemoryThresholdLimit(fileUploadInMemoryThresholdLimit);
  }

  @Override
  public MemoryUnit getFileUploadMemoryUnit() {
    return get().getFileUploadMemoryUnit();
  }

  @Override
  public void setFileUploadMemoryUnit(MemoryUnit fileUploadMemoryUnit) {
    get().setFileUploadMemoryUnit(fileUploadMemoryUnit);
  }

  @Override
  public File getFileUploadTempFileDir() {
    return get().getFileUploadTempFileDir();
  }

  @Override
  public void setFileUploadTempFileDir(File fileUploadTempFileDir) {
    get().setFileUploadTempFileDir(fileUploadTempFileDir);
  }

  @Override
  public HttpClient getHttpClient() {
    return get().getHttpClient();
  }

  @Override
  public HttpContext getHttpContext() {
    return get().getHttpContext();
  }

  @Override
  public void setHttpContext(HttpContext httpContext) {
    get().setHttpContext(httpContext);
  }

  @Override
  public void setSslContext(SSLContext sslContext) {
    get().setSslContext(sslContext);
  }

  @Override
  public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
    get().setHostnameVerifier(hostnameVerifier);
  }

  @Override
  public HttpHost getDefaultProxy() {
    return get().getDefaultProxy();
  }

  @Override
  public boolean isClosed() {
    return get().isClosed();
  }

  @Override
  public void finalize() throws Throwable {
    get().finalize();
  }

}
