package com.suisrc.jaxrsapi.test.bean.impl;

import javax.inject.Named;
import com.suisrc.core.utils.CdiUtils;
import com.suisrc.jaxrsapi.test.bean.TReviseHandler;
import com.suisrc.jaxrsapi.test.bean.T4Str;
import java.lang.String;
import javax.enterprise.context.ApplicationScoped;
import java.lang.Exception;
import com.suisrc.jaxrsapi.test.bean.TLProxy;
import com.suisrc.jaxrsapi.test.bean.RetryPredicateImpl;
import com.suisrc.jaxrsapi.core.ApiActivator;
import com.suisrc.jaxrsapi.test.bean.TestBean;
import com.suisrc.jaxrsapi.core.ServiceClient;
import java.lang.NullPointerException;
import com.suisrc.jaxrsapi.test.bean.TestRest;
import javax.ws.rs.client.WebTarget;
import com.suisrc.jaxrsapi.core.factory.Transform;
import java.lang.Override;
import com.suisrc.jaxrsapi.core.proxy.ProxyBuilder;

/**
 * Follow the implementation of the restful 2.0 standard remote access agent.
 * <see>
 *   https://suisrc.github.io/jaxrsapi
 * <generateBy>
 *   com.suisrc.jaxrsapi.core.factory.ClientServiceFactory
 * <time>
 *   2018-08-07T16:25:50.687
 * <author>
 *   Y13
 */
@ApplicationScoped
public class TestRest_jaxrsapi implements TestRest, ServiceClient {
    /*
     * 远程代理访问客户端控制器
     */
    private TestRest proxy;
    /*
     * 远程服务器控制器，具有服务器信息
     */
    private ApiActivator activator;
    /**
     * 初始化
     */
    public void postConstruct() {
        WebTarget target = ((WebTarget)activator.getAdapter((String)null, WebTarget.class)).path("test");
        proxy = ProxyBuilder.builder(TestRest.class, target).build();
    }
    /**
     * 获取远程服务器控制器
     */
    public ApiActivator getActivator() {
        return activator;
    }
    /**
     * 配置远程服务器控制器
     */
    @Named("test")
    public void setActivator() {
        activator = CdiUtils.selectWithQualifier(ApiActivator.class);
        if (activator != null) postConstruct();

    }
    /**
     * 构造方法
     */
    public TestRest_jaxrsapi() {
        setActivator();
    }
    /**
     * 接口实现
     */
    @Override
    public String getApi_1(TestBean pm0) {
        if (pm0.getName() == null) {
            String temp = (String)activator.getAdapter("t1", String.class);
            if (temp != null) pm0.setName(temp);

        }
        if (pm0.getAge() == null) {
            String temp = (String)activator.getAdapter("T:t2.toString", String.class);
            if (temp != null) pm0.setAge(temp);

        }
        if (pm0.getOther() == null) {
            String temp = (String)activator.getAdapter("t3", String.class);
            if (temp != null) pm0.setOther(temp);

        }
        if (pm0.getOther2() == null) {
            String temp = (String)activator.getAdapter("t4", String.class);
            if (temp != null) pm0.setOther2(temp);

        }
        if (pm0.getOther() == null) {
            String temp = (String)Transform.tf(T4Str.class, "t3");
            if (temp != null) pm0.setOther(temp);

        }
        if (pm0.getOther2() == null) {
            String temp = (String)Transform.tf(String.class, "t4");
            if (temp != null) pm0.setOther2(temp);

        }
        if (pm0.getAge() == null) throw new NullPointerException("年龄为空");

        pm0.setName((new TReviseHandler()).accept(pm0.getName()));
        RetryPredicateImpl predicate = new RetryPredicateImpl(activator);
        int count = 0x10;
        String result;
        Exception exception;
        do {
            result = null;
            exception = null;
            try {
                result = (new TReviseHandler()).accept(proxy.getApi_1(pm0));
            } catch (Exception e) {
                exception = e;
            }
        } while (predicate.test(0x10, --count, result, exception) && count > 0);
        return result;
    }
    /**
     * 接口实现
     */
    @Override
    public String getApi_5(String pm0) {
        if (pm0 == null) {
            String temp = (String)activator.getAdapter("T:grant_type", String.class);
            if (temp != null) pm0 = temp;

        }
        if (pm0 == null) throw new NullPointerException("令牌类型为空");

        pm0 = (new TReviseHandler()).accept(pm0);
        return (String)(new TReviseHandler()).accept((new TLProxy()).hello(activator.getBaseUrl() + "/test" + "/cgi-bin/token", pm0));
    }
}
