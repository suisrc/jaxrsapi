package com.suisrc.jaxrsapi.core.task;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;

import com.suisrc.core.Global;
import com.suisrc.core.scheduler.ScheduleHandler;
import com.suisrc.core.scheduler.Scheduled;
import com.suisrc.jaxrsapi.client.filter.MonitorClientInvokerFilter;
import com.suisrc.jaxrsapi.client.proxy.ProxyBuilder;

/**
 * <p> 一次加载，配置全局jaxrsapi访问控制调度监控器
 * @author Y13
 *
 */
@Scheduled(name=JaxrsapiMonitorTask.MONITOR, keep=false, onStartupKey="application.schedule.jaxrsapi.monitor-valid")
@ApplicationScoped
public class JaxrsapiMonitorTask implements ScheduleHandler {
  public static final String MONITOR = "JAXRSAPI_GLOBAL_MONITOR_01";

  /**
   * <p> 配置全局jaxrsapi调度显示器
   */
  @Override
  public void run() {
    try {
      Function<Class<?>, ?> func = iface -> new MonitorClientInvokerFilter(iface.getName());
      Global.putCacheSafe(Global.getScCache(), ProxyBuilder.GLOBAL_FILTER_GETTER, func);
      Global.getLogger().info("完成全局JAXRSAPI监控器配置：" + MonitorClientInvokerFilter.class.getName());
    } catch (Exception e) {
      // do nothing
      e.printStackTrace();
    }
  }
}
