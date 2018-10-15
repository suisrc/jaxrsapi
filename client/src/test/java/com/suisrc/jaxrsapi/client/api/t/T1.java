package com.suisrc.jaxrsapi.client.api.t;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.MessageBodyReader;

import org.junit.Test;

import com.suisrc.core.fasterxml.FF;
import com.suisrc.core.fasterxml.FasterFactory.Type;
import com.suisrc.jaxrsapi.soap.provider.JacksonClientSoap12Provider;
import com.suisrc.jaxrsapi.soap.provider.Soap12XmlMethod;
import com.suisrc.jaxrsapi.soap.provider.SoapClientUtils;

@Soap12XmlMethod(operationName = "opt123", operationResponse = "opt123", namespace = "http://tempurl.org")
public class T1 {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void test1() throws WebApplicationException, IOException {
        JacksonClientSoap12Provider provider = new JacksonClientSoap12Provider();
        
        
        SzsjjjdRequest req = new SzsjjjdRequest();
        req.setLicensekey("123");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        provider.writeTo(req, req.getClass(), null, T1.class.getAnnotations(), null, null, out);
        System.out.println(out.toString());
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        
        MessageBodyReader<SzsjjjdRequest> reader = (MessageBodyReader)provider;
        SzsjjjdRequest req2 = reader.readFrom(SzsjjjdRequest.class, null, T1.class.getAnnotations(), null, null, in);
        System.out.println(req2);
        String info = FF.getDefault().bean2String(req2, Type.JSON);
        System.out.println(info);
    }
    
    @Test
    public void test2() {
        System.setProperty(JacksonClientSoap12Provider.class.getName(), "true");
        
        SzsjjjdService service = SoapClientUtils.getSoap12ApiByRestful("http://127.0.0.1:8080", SzsjjjdService.class, null, null);
        SzsjjjdRequest req = new SzsjjjdRequest();
        req.setLicensekey("你好");
        try {
            SzsjjjdResult res = service.postesalescreate(req);
            String info = FF.getDefault().bean2String(res, Type.JSON);
            System.out.println(info);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
