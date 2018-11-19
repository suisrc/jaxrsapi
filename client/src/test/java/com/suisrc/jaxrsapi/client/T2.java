package com.suisrc.jaxrsapi.client;


import javax.ws.rs.client.Client;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.suisrc.core.fasterxml.FF;
import com.suisrc.core.fasterxml.FasterFactory.Type;
import com.suisrc.jaxrsapi.client.api.SZSJJJDBody;
import com.suisrc.jaxrsapi.client.api.SZSJJJDResult;
import com.suisrc.jaxrsapi.client.api.SZSJJJDSaleService;
import com.suisrc.jaxrsapi.client.api.SaleInfoRest;
import com.suisrc.jaxrsapi.client.api.ZYHBody;
import com.suisrc.jaxrsapi.client.api.ZYHResult;
import com.suisrc.jaxrsapi.client.filter.MonitorClientInvokerFilter;
import com.suisrc.jaxrsapi.client.filter.MonitorRequestFilter;

/**
 * 
 * @author Y13
 *
 */
public class T2 {
    
    /**
     * 
     */
    @Test
    public void test1() {
        Client client = ClientUtils.getClientWithProvider();

        MonitorClientInvokerFilter filter = new MonitorClientInvokerFilter("深圳卓越汇店");
        String url = "http://218.17.234.114:1235";
        url = "http://127.0.0.1:87711";
        SaleInfoRest rest = ClientUtils.getRestfulApiImplWithFilter(url, SaleInfoRest.class, client, filter);
        
//        client.register(new MonitorRequestFilter("深圳卓越汇店"));
//        client.register(new MonitorResponseFilter("深圳卓越汇店"));
//        String url = "http://218.17.234.114:1235";
//        url = "http://127.0.0.1:8771";
//        SaleInfoRest rest = ClientUtils.getRestfulApiImpl(url, SaleInfoRest.class, client);
        
        ZYHBody body = new ZYHBody();
        body.setShopCode("小傻瓜");
        
        ZYHResult res = rest.saveZYHSaleInfo(body);
        
        System.out.println("返回值：");
        String content = FF.getDefault().bean2String(res, Type.JSON);
        System.out.println(content);
    }
    
    @Test
    public void test2() {
        Client client = ClientUtils.getClientWithProvider();
        
        client.register(new MonitorRequestFilter("深圳卓越汇店"));
        String url = "http://218.17.234.114:1235";
        url = "http://127.0.0.1:8771";
        SZSJJJDSaleService service = ClientUtils.getRestfulApiImpl(url, SZSJJJDSaleService.class, client);
        
        SZSJJJDBody body = new SZSJJJDBody();
        body.setLicensekey("小傻瓜");
        
        SZSJJJDResult res = service.updateSaleInfo(body);
        
        System.out.println("返回值：");
        String content = FF.getDefault().bean2String(res, Type.JSON);
        System.out.println(content);
    }

    @Test
    public void test3() throws SOAPException, JsonProcessingException {
        SZSJJJDBody body = new SZSJJJDBody();
        body.setLicensekey("小傻瓜");
        
        ObjectMapper objectMapper = new ObjectMapper();
        SOAPFactory fac = SOAPFactory.newInstance();
        Detail detailElement = fac.createDetail();
        detailElement.setTextContent("test");
        String result = objectMapper.writer().writeValueAsString(detailElement);
        System.out.println(result);
    }
    
    @Test
    public void test4() throws SOAPException, JsonProcessingException {
        SZSJJJDBody body = new SZSJJJDBody();
        body.setLicensekey("小傻瓜");
        
        ObjectMapper mapper = new XmlMapper();
        String result = mapper.writer().writeValueAsString(body);
        System.out.println(result);
    }
}
