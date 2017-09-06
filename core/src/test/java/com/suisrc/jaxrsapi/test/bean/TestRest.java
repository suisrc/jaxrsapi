package com.suisrc.jaxrsapi.test.bean;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.suisrc.jaxrsapi.core.annotation.LocalProxy;
import com.suisrc.jaxrsapi.core.annotation.OneTimeProxy;
import com.suisrc.jaxrsapi.core.annotation.RemoteApi;
import com.suisrc.jaxrsapi.core.annotation.Reviser;
import com.suisrc.jaxrsapi.core.annotation.ThreadValue;

/**
 * 测试接口
 * @author Y13
 *
 */
@RemoteApi
@OneTimeProxy
public interface TestRest {

    /**
     * 普通接口
     */
    @GET
    @Path("cgi-bin/token")
    @Produces(MediaType.APPLICATION_JSON)
    String getApi_1(@BeanParam TestBean grantType);
    /**
     * ThreadValue, Value, DefaultValue使用
     */
    @LocalProxy(value = TLProxy.class, method = "hello")
    @GET
    @Path("cgi-bin/token")
    @Produces(MediaType.APPLICATION_JSON)
    @Reviser(TReviseHandler.class)
    String getApi_5(@Reviser(TReviseHandler.class)@ThreadValue("grant_type") String grantType);
}
