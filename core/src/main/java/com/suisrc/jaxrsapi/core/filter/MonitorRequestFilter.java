package com.suisrc.jaxrsapi.core.filter;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import com.suisrc.core.utils.ReflectionUtils;
import com.suisrc.jaxrsapi.core.ApiActivator;

/**
 * 请求监控控制器
 * 
 * @author Y13
 *
 */
public class MonitorRequestFilter implements ClientRequestFilter {
    
    /**
     * 服务器激活器
     */
    @SuppressWarnings("unused")
    private ApiActivator activator;
    
    public MonitorRequestFilter(ApiActivator activator) {
        this.activator = activator;
    }

    /**
     * 
     */
    @Override
    public void filter(ClientRequestContext rtx) throws IOException {
        StringBuilder sbir = new StringBuilder();
        sbir.append("↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓\n");
        sbir.append(rtx.getMethod()).append(' ').append(rtx.getUri());
        //--------------------增加header内容
        sbir.append("\n----------header----------\n");
        rtx.getStringHeaders().forEach((k, v) -> sbir.append(k).append(" : ").append(v).append('\n'));
        //--------------------增加请求的内容
        if (rtx.hasEntity()) {
            sbir.append("----------entity----------\n");
            sbir.append(ReflectionUtils.testPrint(rtx.getEntity())).append('\n');
        }
        sbir.append("↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑");
        printRequestInfo(sbir.toString());
    }

    /**
     * 
     * @param string
     */
    protected void printRequestInfo(String info) {
        System.out.println(info);
    }

}
