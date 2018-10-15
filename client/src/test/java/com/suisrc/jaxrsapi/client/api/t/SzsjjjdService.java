package com.suisrc.jaxrsapi.client.api.t;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.suisrc.jaxrsapi.core.annotation.RemoteApi;
import com.suisrc.jaxrsapi.soap.provider.Soap12XmlMethod;
import com.suisrc.jaxrsapi.soap.provider.SoapConsts;

@RemoteApi
public interface SzsjjjdService {

    @Path("api")
    @POST
    @Produces(SoapConsts.APPLICATION_SOAP12_XML)
    @Consumes(SoapConsts.APPLICATION_SOAP12_XML)
    @Soap12XmlMethod(operationName = "postesalescreate", namespace="http://tempurl.org" , prefix = "ns1"/*, prefixAll = true */)
    SzsjjjdResult postesalescreate (SzsjjjdRequest request);

}
