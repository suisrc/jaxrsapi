package com.suisrc.jaxrsapi.test.bean;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.suisrc.jaxrsapi.core.annotation.LocalProxy;
import com.suisrc.jaxrsapi.core.annotation.NonProxy;
import com.suisrc.jaxrsapi.core.annotation.RemoteApi;
import com.suisrc.jaxrsapi.core.annotation.Reviser;
import com.suisrc.jaxrsapi.core.annotation.ThreadValue;
import com.suisrc.jaxrsapi.core.annotation.Value;

/**
 * 测试接口
 * @author Y13
 *
 */
@RemoteApi
public interface TestRest3 {

    /**
     * 普通接口
     */
    @GET
    @Path("cgi-bin/token")
    @Produces(MediaType.APPLICATION_JSON)
    String getApi_1(@QueryParam("grant_type") String grantType, @QueryParam("appid") String appid,  @QueryParam("secret") String secret);
    
    /**
     * ThreadValue, Value, DefaultValue使用
     */
    @GET
    @Path("cgi-bin/token")
    @Produces(MediaType.APPLICATION_JSON)
    String getApi_2(@ThreadValue("grant_type") String grantType, @Value("appid") String appid,  @DefaultValue("secret") String secret);
    
    /**
     * ThreadValue, Value, DefaultValue使用
     */
    @NonProxy("true")
    @GET
    @Path("cgi-bin/token")
    @Produces(MediaType.APPLICATION_JSON)
    String getApi_3(@ThreadValue("grant_type") String grantType, @Value("appid") String appid,  @DefaultValue("secret") String secret);
    
    /**
     * ThreadValue, Value, DefaultValue使用
     */
    @LocalProxy(value = TLProxy.class, method = "hello")
    @GET
    @Path("cgi-bin/token")
    @Produces(MediaType.APPLICATION_JSON)
    String getApi_4(@ThreadValue("grant_type") String grantType, @Value("appid") String appid,  @DefaultValue("secret") String secret);
    
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
