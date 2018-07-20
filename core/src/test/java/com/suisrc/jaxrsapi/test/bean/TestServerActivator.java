package com.suisrc.jaxrsapi.test.bean;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.google.common.collect.Sets;
import com.suisrc.jaxrsapi.core.ApiActivator;

/**
 * 测试
 * 程序入口配置
 * @author Y13
 * https://api.weixin.qq.com/cgi-bin
 */
@Named("test")
@ApplicationScoped
public class TestServerActivator implements ApiActivator {

    @Override
    public String getBaseUrl() {
        return "https://api.weixin.qq.com/cgi-bin";
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Sets.newHashSet(TestRest.class);
    }
    
    @Override
    public boolean isStdInject() {
        return false;
    }

    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(String key, Class<T> type) {
        if (key.equals("false")) {
            return (T)Boolean.FALSE;
        } else if (key.equals("true")) {
            return (T)Boolean.TRUE;
        }
        if (key.equals("T-NAME")) {
            return (T) "XX";
        }
        if (key.equals("T-XX")) {
            return (T) "hello, world";
        }
        if (key.equals("XXX")) {
            return (T) "T-XX";
        }
        return ApiActivator.super.getAdapter(key, type);
    }
    
    public static void main(String[] args) {
        TestServerActivator tsa = new TestServerActivator();
        String value = tsa.getAdapter("T-{T-NAME}", String.class);
        System.out.println(value);
        value = tsa.getAdapter("T-XX", String.class);
        System.out.println(value);
        value = tsa.getAdapter("{XXX}", String.class);
        System.out.println(value);
    }
}
