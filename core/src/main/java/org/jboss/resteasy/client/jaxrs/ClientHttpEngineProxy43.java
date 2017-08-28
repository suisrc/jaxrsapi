package org.jboss.resteasy.client.jaxrs;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
import org.jboss.resteasy.client.jaxrs.engines.factory.ApacheHttpClient4EngineFactory;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;

/**
 * 远程访问执行代理 确保访问进行时候的线程安全 用于取代ApacheHttpClient43Engine
 * 
 * @author Y13
 *
 */
public class ClientHttpEngineProxy43 extends ApacheHttpClient43Engine implements ClientHttpEngine {

    /**
     * 解决多线程问题 每一个线程上都会有一个http客户端引擎
     */
    private ThreadLocal<ApacheHttpClient43Engine> local = new ThreadLocal<>();

    protected RequestConfig.Builder rcBuilder;

    protected HttpHost proxy;

    protected int responseBufferSize;

    protected HostnameVerifier hostnameVerifier;

    protected SSLContext sslContext;

    protected Registry<ConnectionSocketFactory> registry;

    protected int connectionPoolSize;

    protected TimeUnit connectionTTLUnit;

    protected long connectionTTL;

    protected int maxPooledPerRoute;

    public ClientHttpEngineProxy43 setRequestConfigBuilderSafe(RequestConfig.Builder rcBuilder) {
        this.rcBuilder = rcBuilder;
        return this;
    }

    public ClientHttpEngineProxy43 setProxySafe(HttpHost proxy) {
        this.proxy = proxy;
        return this;
    }

    public ClientHttpEngineProxy43 setResponseBufferSizeSafe(int responseBufferSize) {
        this.responseBufferSize = responseBufferSize;
        return this;
    }

    public ClientHttpEngineProxy43 setHostnameVerifierSafe(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    public ClientHttpEngineProxy43 setSslContextSafe(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    public ClientHttpEngineProxy43 setRegistrySafe(Registry<ConnectionSocketFactory> registry) {
        this.registry = registry;
        return this;
    }

    public ClientHttpEngineProxy43 setConnectionPoolSizeSafe(int connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
        return this;
    }

    public ClientHttpEngineProxy43 setConnectionTTLUnitSafe(TimeUnit connectionTTLUnit) {
        this.connectionTTLUnit = connectionTTLUnit;
        return this;
    }

    public ClientHttpEngineProxy43 setConnectionTTLSafe(long connectionTTL) {
        this.connectionTTL = connectionTTL;
        return this;
    }

    public ClientHttpEngineProxy43 setMaxPooledPerRouteSafe(int maxPooledPerRoute) {
        this.maxPooledPerRoute = maxPooledPerRoute;
        return this;
    }

    protected ApacheHttpClient43Engine get() {
        ApacheHttpClient43Engine engine = local.get();
        if (engine == null) {
            engine = createClientHttpEngine();
            local.set(engine);
        }
        return engine;
    }

    protected ApacheHttpClient43Engine createClientHttpEngine() {
        // 构建http访问客户端
        HttpClient httpClient = HttpClientBuilder.create().setConnectionManager(createHttpClientConnectionManager())
                .setDefaultRequestConfig(rcBuilder.build()).setProxy(proxy).disableContentCompression().build();
        // 构建http访问引擎
        ApacheHttpClient43Engine engine = (ApacheHttpClient43Engine) ApacheHttpClient4EngineFactory.create(httpClient, true);
        engine.setResponseBufferSize(responseBufferSize);
        engine.setHostnameVerifier(hostnameVerifier);
        // this may be null. We can't really support this with Apache Client.
        engine.setSslContext(sslContext);
        return engine;
    }

    protected HttpClientConnectionManager createHttpClientConnectionManager() {
        if (connectionPoolSize > 0) {
            PoolingHttpClientConnectionManager tcm =
                    new PoolingHttpClientConnectionManager(registry, null, null, null, connectionTTL, connectionTTLUnit);
            tcm.setMaxTotal(connectionPoolSize);
            tcm.setDefaultMaxPerRoute(maxPooledPerRoute);
            return tcm;
        } else {
            return new BasicHttpClientConnectionManager(registry);
        }
    }

    // -------------------------------------ClientHttpEngine--------------------------------//

    @Override
    public SSLContext getSslContext() {
        return get().getSslContext();
        // return sslContext;
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return get().getHostnameVerifier();
        // return hostnameVerifier;
    }

    @Override
    public ClientResponse invoke(ClientInvocation request) {
        return get().invoke(request);
    }

    @Override
    public void close() {
        get().close();
    }

    // -------------------------------------ApacheHttpClient43Engine--------------------------------//

    @Override
    public HttpHost getDefaultProxy() {
        return get().getDefaultProxy();
    }

    // -------------------------------------ApacheHttpClient4Engine--------------------------------//

    // @Override
    // public SSLContext getSslContext() {
    // return get().getSslContext();
    // }
    //
    // @Override
    // public HostnameVerifier getHostnameVerifier() {
    // return get().getHostnameVerifier();
    // }
    //
    // @Override
    // public HttpHost getDefaultProxy() {
    // return get().getDefaultProxy();
    // }
    //
    // @Override
    // public ClientResponse invoke(ClientInvocation request) {
    // return get().invoke(request);
    // }
    //
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
    public boolean isClosed() {
        return get().isClosed();
    }

    @Override
    public void finalize() throws Throwable {
        get().finalize();
    }

}
