package com.suisrc.jaxrsapi.core;

import java.util.Set;

import javax.inject.Named;

/**
 * 适配器代理
 * 
 * @author Y13
 *
 */
public class ApiActivatorProxy implements ApiActivatorInfo {
    
    private ApiActivator target;
    
    public ApiActivatorProxy(ApiActivator target) {
        this.target = target;
    }

    @Override
    public <T> T getAdapter(String key, Class<T> type) {
        return target.getAdapter(key, type);
    }

    @Override
    public boolean isMulitMode() {
        return target.isMulitMode();
    }

    @Override
    public boolean isStdInject() {
        return target.isStdInject();
    }

    @Override
    public Integer getApiPriority() {
        return target.getApiPriority();
    }

    @Override
    public String getActivatorName() {
        Named named = target.getClass().getAnnotation(Named.class);
        if (named == null) {
                throw new RuntimeException("Not found 'Named' annotation: " + target.getClass());
        }
        return named.value();
    }

    @Override
    public Set<Class<?>> getClasses() {
        return target.getClasses();
    }

    @Override
    public String getActivatorClassName() {
        return target.getClass().getSimpleName();
    }

    @Override
    public String getActivatorPackageName() {
        return target.getClass().getPackage().getName();
    }
}
