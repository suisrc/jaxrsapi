package com.suisrc.jaxrsapi.client.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import com.suisrc.core.fasterxml.FF;
import com.suisrc.core.fasterxml.FasterFactory;
import com.suisrc.core.fasterxml.FasterFactory.Type;

public class MonitorResponseFilter implements ClientResponseFilter {
  private static final Logger logger = Logger.getLogger(MonitorRequestFilter.class.getName());

  /**
   * <p> 请求名称
   */
  private String name;

  /**
   * 
   * @param activator
   */
  public MonitorResponseFilter(String name) {
    this.name = name;
  }

  /**
   * <p> 请求的名称
   * 
   * @return
   */
  public String getRequestName() {
    return name;
  }

  /**
   * 
   */
  @Override
  public void filter(ClientRequestContext rtx, ClientResponseContext rcx) throws IOException {
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
    sbir.append("----------result----------\n");
    sbir.append("status : ").append(rcx.getStatus());
    sbir.append("length : ").append(rcx.getLength());
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
