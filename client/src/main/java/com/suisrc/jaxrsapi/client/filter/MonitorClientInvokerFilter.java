package com.suisrc.jaxrsapi.client.filter;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.resteasy.client.jaxrs.internal.proxy.ClientInvoker;

import com.suisrc.core.fasterxml.FF;
import com.suisrc.core.fasterxml.FasterFactory;
import com.suisrc.core.fasterxml.FasterFactory.Type;
import com.suisrc.jaxrsapi.client.proxy.ClientInvokerFilter;

/**
 * <p> 拦截器
 * 
 * @author Y13
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MonitorClientInvokerFilter implements ClientInvokerFilter {
  private static final Logger logger = Logger.getLogger(MonitorRequestFilter.class.getName());

  private static final String MONITOR_CLIENT_REQ = "MONITOR_CLIENT_REQ_1990";
  private static final String MONITOR_CLIENT_RES = "MONITOR_CLIENT_RES_1990";

  private String name;

  private FasterFactory ff = FF.getDefault();

  public MonitorClientInvokerFilter(String name) {
    this.name = name;
  }

  public String getRequestName() {
    return name;
  }

  @Override
  public Map getCache() {
    return new HashMap();
  }

  public Object before(Map cache, ClientInvoker invoker, ClientInvocation request) {
    cache.put(MONITOR_CLIENT_REQ, request);
    return null;
  }

  public Object after(Map cache, ClientInvoker invoker, ClientResponse response) {
    cache.put(MONITOR_CLIENT_RES, response);
    return null;
  }

  public void finally0(Map cache, ClientInvoker invoker) {}

  public Object entity0(Map cache, ClientInvoker invoker, Object result) {
    invoke(cache, result, null);
    return null;
  }

  public Object exception0(Map cache, ClientInvoker invoker, Exception ex) {
    invoke(cache, null, ex);
    return null;
  }

  private void invoke(Map cache, Object result, Exception ex) {
    try {
      ClientInvocation req = (ClientInvocation) cache.get(MONITOR_CLIENT_REQ);
      ClientResponse res = (ClientResponse) cache.get(MONITOR_CLIENT_RES);

      StringBuilder sbir = new StringBuilder();
      sbir.append("execute remote access request by [" + getRequestName() + "]\n");
      sbir.append("↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓\n");
      if (req != null) {
        sbir.append(req.getMethod()).append(' ').append(req.getUri());
        // --------------------增加header内容
        sbir.append("\n---------------------------------------------------------------request---------\n");
        req.getHeaders().asMap().forEach((k, v) -> sbir.append(k).append(" : ").append(v).append('\n'));
        // --------------------增加请求的内容
        if (req.getEntity() != null) {
          sbir.append("--------------------------------------------------------------------------------\n");
          if (OutputStream.class.isAssignableFrom(req.getEntityClass())) {
            sbir.append("stream data").append('\n');
          } else {
            String content = ff.convert2String(ff.getObjectMapper(Type.JSON), true, req.getEntity());
            sbir.append(content + "\n");
          }
        }
      }
      if (res != null) {
        sbir.append("--------------------------------------------------------------response---------\n");
        sbir.append("status : ").append(res.getStatus()).append('\n');
        if (result != null) {
          sbir.append("--------------------------------------------------------------------------------\n");
          if (result instanceof InputStream) {
            sbir.append("stream data").append('\n');
          } else {
            String content = ff.convert2String(ff.getObjectMapper(Type.JSON), true, result);
            sbir.append(content + "\n");
          }
        }
      }
      if (ex != null) {
        sbir.append("------------------------------------------------------------exception---------\n");
        sbir.append("exception : " + ex.getMessage() + "\n");
      }
      sbir.append("↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑");
      printRequestInfo(sbir.toString());
    } catch (Exception e) {
      e.printStackTrace();
      // do nothing
    }
  }


  /**
   * 
   * @param string
   */
  protected void printRequestInfo(String info) {
    logger.info(info);
  }
}
