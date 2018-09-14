package com.suisrc.jaxrsapi.core.retry;

import java.util.concurrent.Callable;

import com.suisrc.core.Global;
import com.suisrc.core.utils.Throwables;
import com.suisrc.jaxrsapi.core.JaxrsConsts;
import com.suisrc.jaxrsapi.core.ServiceClient;
import com.suisrc.jaxrsapi.core.runtime.RetryPredicate;

/**
 * 代理和重定向重试结果
 * 
 * @author Y13
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ProxyRetryPredicate implements RetryPredicate<Object> {

    /**
     * 执行代理
     * 
     * 对返回值的内容需要强制限定
     * 
     * @throws Exception 
     */
    public static <T> T proxy(RetryPredicate<T> predicate, Callable<T> callable) {
        return Global.execByTempValue(JaxrsConsts.PROXY_RETRY_PREDICATE, predicate, callable);
    }
    /**
     * 执行代理
     * @throws Exception 
     */
    public static void proxy(RetryPredicate predicate, Runnable runnable) {
        Global.execByTempValue(JaxrsConsts.PROXY_RETRY_PREDICATE, predicate, runnable);
    }

    /**
     * 激活器
     */
    private final ServiceClient clinet;
    
    /**
     * 代理实体
     */
    private RetryPredicate target = null;
    
    /**
     * 构造方法
     * @param activator
     */
    public ProxyRetryPredicate() {
        this.clinet = null;
    }
    
    public ProxyRetryPredicate(ServiceClient clinet) {
        this.clinet = clinet;
    }

    /**
     * 断言方法
     */ 
    @Override
    public boolean test(int count, int time, Object result, Exception e) {
        if (target == null) {
            target = (RetryPredicate) Global.getThreadCache().get(JaxrsConsts.PROXY_RETRY_PREDICATE);
            if (target == null) {
                if (e != null) {
                    // 抛出异常， 内容无法处理
                    throw Throwables.getRuntimeException(e);
                }
                return false;
            }
            if (clinet != null && target instanceof ProxyRetryPredicate2) {
                // 第一次执行, 对激活器进行赋值
                ((ProxyRetryPredicate2)target).setServiceClient(clinet);
            }
        }
        return target.test(count, time, result, e);
    }

}
