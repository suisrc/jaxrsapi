package com.suisrc.jaxrsapi.client.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import com.suisrc.core.fasterxml.FF;
import com.suisrc.core.fasterxml.FasterFactory;
import com.suisrc.core.fasterxml.FasterFactory.Type;

/**
 * 请求监控控制器
 * 
 * @author Y13
 *
 */
public class MonitorRequestFilter implements ClientRequestFilter {
    private static final Logger logger = Logger.getLogger(MonitorRequestFilter.class.getName());
    
    /**
     * 请求名称
     */
    private String name;
    
    /**
     * 
     * @param activator
     */
    public MonitorRequestFilter(String name) {
        this.name = name;
    }
    
    /**
     * 请求的名称
     * @return
     */
    public String getRequestName() {
        return name;
    }

    /**
     * 
     */
    @Override
    public void filter(final ClientRequestContext rtx) throws IOException {
        StringBuilder sbir = new StringBuilder();
        sbir.append("execute remote access request by [" + getRequestName() + "]\n");
        sbir.append("↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓\n");
        sbir.append(rtx.getMethod()).append(' ').append(rtx.getUri());
        // --------------------增加header内容
        sbir.append("\n----------header----------\n");
        rtx.getStringHeaders().forEach((k, v) -> sbir.append(k).append(" : ").append(v).append('\n'));
        // --------------------增加请求的内容
        if (rtx.hasEntity()) {
            sbir.append("----------entity----------\n");
            if (OutputStream.class.isAssignableFrom(rtx.getEntityClass())) {
                sbir.append("stream data").append('\n');
            } else {
                FasterFactory f = FF.getDefault();
                String content = f.convert2String(f.getObjectMapper(Type.JSON), true, rtx.getEntity());
                sbir.append(content + "\n");
            }
        }
        sbir.append("↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑");
        printRequestInfo(sbir.toString());
    }

    /**
     * 
     * @param string
     */
    protected void printRequestInfo(String info) {
        logger.info(info);
    }

}
