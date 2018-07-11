package com.suisrc.jaxrsapi.core.simple;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.suisrc.core.ScCDI;
import com.suisrc.jaxrsapi.core.AccessTokenActivator;
import com.suisrc.jaxrsapi.core.token.Token;

/**
 * 该接口仅仅作为测试参考
 * 
 * @author Y13
 *
 */
@ApplicationScoped
@Named("SIMPLE_TOKEN_ACTIVATOR")
public class SimpleTokenActivator extends AccessTokenActivator {
    
    private TokenProduce tokenProduce;
    
    private String appId;
    
    private String appSecret;

    /**
     * 该类只有在代理生成所用，运行时无用
     */
    @Override
    public Set<Class<?>> getClasses() {
        return null;
    }

    @Override
    public String getAppName() {
        return "Simple";
    }
    
    @Override
    public void postConstruct() {
        appId = System.getProperty("com.suisrc.activators.simple.appId");
        appSecret = System.getProperty("com.suisrc.activators.simple.appSecret");
        super.postConstruct();
    }

    @Override
    protected String getBaseUrlKey() {
        return "com.suisrc.activators.simple.appUrl";
    }

    @Override
    @Named("SIMPLE_TOKEN_ACTIVATOR")
    protected Token getTokenByRemote() {
        if (tokenProduce == null) {
            ScCDI.injectWithNamed(0, SimpleTokenActivator.class, v -> tokenProduce == null, v -> tokenProduce = v, TokenProduce.class);
        }
        return tokenProduce.getToken(appId, appSecret);
    }

}
