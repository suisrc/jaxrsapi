package com.suisrc.jaxrsapi.core;

import java.util.concurrent.ExecutorService;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.jboss.resteasy.client.jaxrs.ClientBuilderFactory;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.suisrc.core.Global;
import com.suisrc.jaxrsapi.core.filter.MonitorRequestFilter;

/**
 * 程序入口配置抽象
 * 
 * @author Y13
 */
public abstract class AbstractActivator implements ApiActivator {
    /**
     * 调试标记
     */
    protected static final boolean DEBUG = Boolean.getBoolean(JaxrsapiConsts.DEBUG);

    /**
     * 提供器工厂，序列化和反序列化对象
     */
    protected ResteasyProviderFactory providerFactory = null;

    /**
     * 多线程访问缓冲，可以通知远程访问到线程池
     */
    protected ExecutorService executor = null;

    /**
     * 访问的客户端，每个激活器一个
     */
    protected Client client;

    /**
     * 访问默认代理主机
     */
    protected String proxyHost = null;
    /**
     * 访问默认代理端口
     */
    protected int proxyPort = -1;

    /**
     * 构造方法
     */
    protected AbstractActivator() {
        doInitConstruct();
    }
    
    // ----------------------------------------------------------------ZERO 通用接口属性索引
    
    /**
     * 构建访问线程池, 默认不使用私有线程池
     * executor = Executors.newFixedThreadPool(10);
     */
    protected ExecutorService createExecutorService() {
//        RefOne<ExecutorService> ref = new RefOne<>();
//        ScCDI.injectWithNamed(0, null, null, ref::set, Exception::printStackTrace, ScExecutor.class);
//        // ref.get 可能为空，但是为空到时候，会输出注入时候发生到空异常
//        return ref.get();
        return Global.getScExecutor();
    }

    /**
     * 初始化远程访问的客户端
     */
    protected Client createTargetClient() {
        ClientBuilder clientBuilder = createClientBuilder();// 配置网络通信内容
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
        registerTargetClient(client);
        return client;
    }

    /**
     * 对访问器注入监听等内容
     * @param client
     */
    protected void registerTargetClient(Client client) {
        client.register(new MonitorRequestFilter(this));
    }

    /**
     * 获取一个Client Builder
     */
    protected ClientBuilder createClientBuilder() {
        // return ClientBuilder.newBuilder();
        return ClientBuilderFactory.newBuilder();
    }

    /**
     * 构造方法调用
     */
    public void doInitConstruct() {
    }
    
    /**
     * 构造后被系统调用 进行内容初始化
     */
    @Override
    public void doPostConstruct() {
        proxyHost = createProxyHost();
        proxyPort = createProxyPort();
        // 执行获取执行到线程池
        executor = createExecutorService();
        // 构建客户端创建器
        client = createTargetClient();
    }
    
    // ----------------------------------------------------------------ZERO Adapter

    /**
     * 获取系统的对象
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Class<T> type) {
        if (type == WebTarget.class) {
            return (T) client.target(getBaseUrl());
        } else if (type == Client.class) {
            return (T) client;
        } else if (type == ResteasyProviderFactory.class) {
            return (T) providerFactory;
        }
        return null;
    }

    /**
     * 主要是为了防止不支持javaee7.0标准的反向内容注入
     */
    @Override
    public <T> void setAdapter(Class<T> type, T value) {
        if (type == ResteasyProviderFactory.class) {
            providerFactory = (ResteasyProviderFactory) value;
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
    
}
