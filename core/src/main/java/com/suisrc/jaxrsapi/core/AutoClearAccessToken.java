package com.suisrc.jaxrsapi.core;

import com.suisrc.core.utils.Throwables;
import com.suisrc.jaxrsapi.core.ApiActivator;
import com.suisrc.jaxrsapi.core.runtime.RetryPredicate;

/**
 * 自动清空访问token
 * 
 * @author Y13
 *
 */
public abstract class AutoClearAccessToken implements RetryPredicate<Object> {

    /**
     * 激活器
     */
    private ApiActivator activator;
    
    /**
     * 构造方法
     * @param activator
     */
    public AutoClearAccessToken(ApiActivator activator) {
        this.activator = activator;
    }

    /**
     * 断言方法
     */
    @Override
    public boolean test(int count, int time, Object result, Exception e) {
        if (time == 0 && e != null) {
            // 最后一次反生异常，不进行处理，将异常抛出
            throw Throwables.getRuntimeException(e);
        }
        if (!(activator instanceof AccessTokenActivator)) {
            // 不是令牌管理的激活器，不进行处理
            return false; //
        }
        if (isTokenExpired(result, e)) {
            // 验证为令牌过期，删除令牌后，重试
            String tokenKey = getTokenKey();
            if (tokenKey == null) {
                ((AccessTokenActivator)activator).clearToken(isClearAutoUpdateService());
            } else {
                ((AccessTokenActivator)activator).clearToken(tokenKey, isClearAutoUpdateService());
            }
            return true;
        }
        if (e != null) {
            throw Throwables.getRuntimeException(e);
        }
        return false;
    }
    
    /**
     * 获取令牌关键字
     * 
     * 如果返回为null, 使用系统级别默认处理
     * @return
     */
    protected String getTokenKey() {
        return null;
    }
    
    /**
     * 如果有自动更新服务，是否一并删除
     */
    protected boolean isClearAutoUpdateService() {
        return false;
    }

    /**
     * 判断令牌是否过期
     * @param result
     * @param e
     * @return
     */
    protected abstract boolean isTokenExpired(Object result, Exception e);
}
