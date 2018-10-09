package com.suisrc.jaxrsapi.client;


import javax.ws.rs.client.Client;

import org.jboss.resteasy.client.jaxrs.internal.LocalResteasyProviderFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.Test;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider;
import com.suisrc.core.fasterxml.FF;
import com.suisrc.core.fasterxml.FasterFactory.Type;
import com.suisrc.jaxrsapi.client.api.SaleInfoRest;
import com.suisrc.jaxrsapi.client.api.ZYHBody;
import com.suisrc.jaxrsapi.client.api.ZYHResult;
import com.suisrc.jaxrsapi.client.filter.MonitorRequestFilter;

/**
 * 
 * @author Y13
 *
 */
public class T2 {
    
    /**
     * 提供器
     * 
     * @return
     */
    public static ResteasyProviderFactory getNativeProviderFactory() {
        // create a new one
        ResteasyProviderFactory providerFactory = new LocalResteasyProviderFactory(ResteasyProviderFactory.newInstance());
        RegisterBuiltin.register(providerFactory);
        providerFactory.registerProvider(JacksonJsonProvider.class, true); // 装载翻译器
        providerFactory.registerProvider(JacksonXMLProvider.class, true); // 装载翻译器
        return providerFactory;
    }

    /**
     * 
     */
    @Test
    public void test1() {
        Client client = ClientUtils.getClientWithProvider();
        
        client.register(new MonitorRequestFilter("深圳卓越汇店"));
        String url = "http://218.17.234.114:1235";
        url = "http://127.0.0.1:8771";
        SaleInfoRest rest = ClientUtils.getRestfulApiImpl(url, SaleInfoRest.class, client);
        
        ZYHBody body = new ZYHBody();
        body.setShopCode("小傻瓜");
        
        ZYHResult res = rest.saveZYHSaleInfo(body);
        
        System.out.println("返回值：");
        String content = FF.getDefault().bean2String(res, Type.JSON);
        System.out.println(content);
    }
}
