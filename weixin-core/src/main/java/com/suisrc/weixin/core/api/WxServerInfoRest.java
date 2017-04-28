package com.suisrc.weixin.core.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.suisrc.jaxrsapi.core.annotation.RemoteApi;
import com.suisrc.jaxrsapi.core.annotation.SystemValue;
import com.suisrc.weixin.core.WxConsts;
import com.suisrc.weixin.core.bean.ServerIpResult;

/**
 * 获取微信服务器信息
 * @author Y13
 *
 */
@RemoteApi
@Path("cgi-bin")
public interface WxServerInfoRest {

	/**
	 * 获取微信服务器IP地址
	 * 如果公众号基于安全等考虑，需要获知微信服务器的IP地址列表，以便进行相关限制，可以通过该接口获得微信服务器IP地址列表或者IP网段信息。
	 * 
	 * https://api.weixin.qq.com/cgi-bin/getcallbackip?access_token=ACCESS_TOKEN
	 * 
	 * @param accessToken 公众号的access_token
	 * @return
	 */
	@GET
	@Path("getcallbackip")
	@Produces(MediaType.APPLICATION_JSON)
	ServerIpResult getCallbackIp(@QueryParam("access_token")@SystemValue(WxConsts.ACCESS_TOKEN) String accessToken);
}