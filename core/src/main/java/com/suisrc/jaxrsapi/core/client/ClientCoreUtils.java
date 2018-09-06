package com.suisrc.jaxrsapi.core.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.jboss.resteasy.client.jaxrs.ClientBuilderFactory;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.suisrc.jaxrsapi.client.ClientUtils;
import com.suisrc.jaxrsapi.core.factory.NativeServiceClientFactory;

/**
 * 
 * @author Y13
 *
 */
public class ClientCoreUtils extends ClientUtils {

    /**
     * 获取访问使用的client
     * @param builderFactory
     * @param clientFactory
     * @return
     */
    public static Client getClientWithProvider() {
        ClientBuilder builder = ClientBuilderFactory.newBuilder();
        ResteasyProviderFactory provider = NativeServiceClientFactory.getNativeProviderFactory();
        ((ResteasyClientBuilder)builder).providerFactory(provider);
        Client client = builder.build();
        return client;
    }
}
