package com.suisrc.jaxrsapi.test.bean;

import com.suisrc.jaxrsapi.core.ApiActivator;

/**
 * 测试本地代理
 * @author Y13
 *
 */
public class TLProxy {
    
    public TLProxy() {}
    
    public TLProxy(ApiActivator aa) {
        
    }
    
    public String hello(String url, String pm) {
        return "hello";
    }
    
    public String hello(String url, String pm0, String pm1, String pm2) {
        return "hello";
    }

}
