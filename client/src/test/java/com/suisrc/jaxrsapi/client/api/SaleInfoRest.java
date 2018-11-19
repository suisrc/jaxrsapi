package com.suisrc.jaxrsapi.client.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.suisrc.jaxrsapi.core.annotation.RemoteApi;

/**
 * <p> 远程访问接口
 * 
 * @author Y13
 *
 */
@RemoteApi
public interface SaleInfoRest {

    /**
     * 深圳卓越汇店
     */
    @POST
    @Path("DataTransfer.asmx")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    ZYHResult saveZYHSaleInfo(ZYHBody body);
}
