package com.suisrc.jaxrsapi.client.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.suisrc.jaxrsapi.core.annotation.RemoteApi;

/**
 * 远程访问接口
 * 
 * @author Y13
 *
 */
@RemoteApi
public interface SZSJJJDSaleService {

    /**
     * 深圳沙井京基店
     */
    @POST
    @Path("TTPOS/sales.asmx")
    @Produces("application/soap+xml")
    @Consumes("application/soap+xml")
    SZSJJJDResult updateSaleInfo(SZSJJJDBody body);
}
