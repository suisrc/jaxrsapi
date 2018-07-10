package com.suisrc.jaxrsapi.test.bean;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.suisrc.jaxrsapi.core.Consts;
import com.suisrc.jaxrsapi.core.annotation.LocalProxy;
import com.suisrc.jaxrsapi.core.annotation.RemoteApi;
import com.suisrc.jaxrsapi.core.annotation.Retry;
import com.suisrc.jaxrsapi.core.annotation.Reviser;
import com.suisrc.jaxrsapi.core.annotation.Value;

/**
 * 测试接口
 * @author Y13
 *
 */
@RemoteApi("test")
//@OneTimeProxy
public interface TestRest {

    /**
     * 普通接口
     */
    @Retry(value=RetryPredicateImpl.class, count = 16, master = Consts.FIELD_ACTIVATOR)
    @Reviser(TReviseHandler.class)
    @GET
    @Path("cgi-bin/token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    String getApi_1(@BeanParam TestBean grantType);
    /**
     * ThreadValue, Value, DefaultValue使用
     */
    @LocalProxy(value = TLProxy.class, method = "hello")
    @Reviser(TReviseHandler.class)
    @GET
    @Path("cgi-bin/token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    String getApi_5(@Reviser(TReviseHandler.class)@Value("T:grant_type") String grantType);
}
