package com.suisrc.jaxrsapi.core;

import javax.inject.Named;

import com.suisrc.core.ScCDI;
import com.suisrc.core.ScKey;
import com.suisrc.core.cache.ScopedCache;
import com.suisrc.jaxrsapi.core.token.Token;
import com.suisrc.jaxrsapi.core.token.TokenReference;

/**
 * 程序入口配置抽象
 * 
 * 该内容为开发完成，请不要使用
 * 
 * @author Y13
 */
@Deprecated
public abstract class SwarmTokenActivator extends AccessTokenActivator {
    
    /**
     * 内部集群Token
     */
    private class SwarmToken extends TokenReference {
        @Override
        public TokenReference set(Token newValue) {
            swarmCache.put(swarmTokenKey, newValue);
            return this;
        }
        @Override
        public Token get() {
            return (Token) swarmCache.get(swarmTokenKey);
        }
    }
    
    /**
     * 集群缓存
     */
    private ScopedCache swarmCache;
    
    /**
     * token key
     */
    private String swarmTokenKey;
    
    @Override
    public void postConstruct() {
        super.postConstruct();
        injectCache();
    }
    
    @Named(ScKey.NAMED_SWARM)
    private void injectCache() {
        swarmCache = null;
        swarmTokenKey = getTokenKeyByTopology();
        ScCDI.injectWithNamed(0, null, null, v -> swarmCache = v, Exception::printStackTrace, ScopedCache.class);
    }

    /**
     * 获取拓扑中的token到key
     * @param key
     * @return
     */
    protected String getTokenKeyByTopology() {
        return getAppId() + "$$->&&" + getAppSecret();
    }
    
    /**
     * 更新拓扑中的token
     * @param key
     * @param accessToken
     * @return
     */
    protected TokenReference putAccessTokenByTopology(TokenReference token) {
        return swarmCache == null ? token : new SwarmToken().set(token.get());
    }

    /**
     * 获取拓扑中的token
     * @param key
     * @return
     */
    protected TokenReference getAccessTokenByTopology() {
        return swarmCache == null || swarmCache.get(swarmTokenKey) == null ? null : new SwarmToken();
    }
}
