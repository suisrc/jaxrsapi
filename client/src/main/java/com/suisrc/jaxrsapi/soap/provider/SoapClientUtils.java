package com.suisrc.jaxrsapi.soap.provider;

import java.util.Map;
import java.util.function.Consumer;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.jboss.resteasy.client.jaxrs.ClientBuilderFactory;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.LocalResteasyProviderFactory;
import org.jboss.resteasy.client.jaxrs.internal.proxy.ClientInvoker;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.suisrc.core.Global;
import com.suisrc.core.cache.ScopedCache;
import com.suisrc.jaxrsapi.client.ClientUtils;
import com.suisrc.jaxrsapi.client.proxy.ClientInvokerFilter;
import com.suisrc.jaxrsapi.client.proxy.ClientInvokerFilterBefore;

/**
 * Soap12访问
 * 
 * 实验性内容
 * 
 * @author Y13
 *
 */
public class SoapClientUtils {

    /**
     * 获取访问使用的client
     * 
     * @param builderFactory
     * @param clientFactory
     * @return
     */
    public static Client getClientWithSoap12() {
        ClientBuilder builder = ClientBuilderFactory.newBuilder();
        // 装载翻译器
        ResteasyProviderFactory providerFactory = new LocalResteasyProviderFactory(ResteasyProviderFactory.newInstance());
        RegisterBuiltin.register(providerFactory);
        providerFactory.registerProvider(JacksonClientSoap12Provider.class, true);
        // 配置
        ((ResteasyClientBuilder) builder).providerFactory(providerFactory);
        Client client = builder.build();
        return client;
    }
    
    /**
     * 专门为Soap12开放的访问代理内容
     * 
     * 该内容处于实验阶段
     * 
     * @param uri
     * @param client
     * @param iface
     * @return
     */
    public static <T> T getSoap12ApiByRestful(String uri, Class<T> iface) {
        return getSoap12ApiByRestful(uri, iface);
    }

    /**
     * 专门为Soap12开放的访问代理内容
     * 
     * 该内容处于实验阶段
     * 
     * @param uri
     * @param client
     * @param iface
     * @return
     */
    public static <T> T getSoap12ApiByRestful(String uri, Class<T> iface, Client client, Consumer<Client> clientConsumer) {
        if (client == null) {
            client = getClientWithSoap12();
            if (clientConsumer != null) {
                clientConsumer.accept(client);
            }
        }
        return ClientUtils.getRestfulApiImplWithFilter(uri, iface, client, setClientInvokerToThread());
    }

    /**
     * 设定访问拦截器到线程上
     * 
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static ClientInvokerFilter setClientInvokerToThread() {
        return new ClientInvokerFilterBefore() {
            private Consumer<String> remover;

            @Override
            public Object before(Map c0, ClientInvoker invoker, ClientInvocation request) {
                ScopedCache cache = Global.getThreadCache();
                if (cache != null) {
                    cache.put(SoapConsts.CLIENT_INVOKER, invoker);
                    remover = s -> cache.remove(SoapConsts.CLIENT_INVOKER);
                } else {
                    Map<Object, Object> cache0 = JacksonClientSoap12Provider.getThreadCache();
                    cache0.put(SoapConsts.CLIENT_INVOKER, invoker);
                    remover = s -> cache0.remove(SoapConsts.CLIENT_INVOKER);
                }
                return null;
            }

            @Override
            public void finally0(Map c0, ClientInvoker invoker) {
                if (remover != null) {
                    remover.accept(null);
                    remover = null;
                }
            }

        };
    }
}
