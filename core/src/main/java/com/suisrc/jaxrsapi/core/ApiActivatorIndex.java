package com.suisrc.jaxrsapi.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public interface ApiActivatorIndex {
    
    /**
     * 实现属性前缀
     */
    public static final String IMPL = "_IMPL";
    
    /**
     * 获取所有接口实现的索引
     * 
     * @return
     */
    @SuppressWarnings("rawtypes")
    default Map<Class<?>, Class<?>> getApiImpl() {
        Class<?> declared = getClass();
        // 把属性值转换为map格式
        Map<String, String> fields = new HashMap<>();
        for (Field field : declared.getFields()) {
            if (field.getName().equals("IMPL")) {
                continue; // 不处理
            }
            int mod = field.getModifiers();
            if (!(Modifier.isStatic(mod) && Modifier.isFinal(mod))) {
                continue; // 非static和final，不处理
            }
            try {
                String name = field.getName();
                Object value = field.get(this);
                if (value != null) {
                    String clazz = value.toString();
                    int offset = clazz.indexOf(JaxrsConsts.separator);
                    if (offset >= 0) {
                        clazz = clazz.substring(offset + 1);
                    }
                    fields.put(name, clazz);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                System.out.println(e.getClass() + ":" + e.getMessage());
            }
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        
        Map<Class<?>, Class<?>> apiImpls = new HashMap<>();
        fields.forEach((apiKey, apiValue) -> {
            if (!apiKey.endsWith(IMPL)) {
                String implKey = apiKey + IMPL;
                String implValue = fields.get(implKey);
                if (implValue != null) {
                    try {
                        Class apiClass = loader.loadClass(apiValue);
                        Class impClass = loader.loadClass(implValue);
                        apiImpls.put(apiClass, impClass);
                    } catch (ClassNotFoundException e) {
                        System.out.println(e.getClass() + ":" + e.getMessage());
                    }
                }
            }
        });
        return apiImpls;
    }

}
