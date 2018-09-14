package com.suisrc.jaxrsapi.core.retry;

import com.suisrc.jaxrsapi.core.ServiceClient;

/**
 * 回调赋值
 * 
 * @author Y13
 *
 */
public interface ProxyRetryPredicate2 {

    /**
     * 回调内容
     * @param clinet
     */
    void setServiceClient(ServiceClient clinet);

}
