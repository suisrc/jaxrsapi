package com.suisrc.jaxrsapi.core.runtime;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.resteasy.client.jaxrs.internal.LocalResteasyProviderFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider;
import com.suisrc.jaxrsapi.core.ApiActivator;
import com.suisrc.jaxrsapi.core.ServiceClient;

import javassist.ClassPool;
import javassist.CtClass;

/**
 * 执行本地处理 系统中静态存放远程访问实体
 * 
 * 该对象应对系统中没有web容器和自动加载机制的系统中使用
 * 
 * @author Y13
 *
 */
@SuppressWarnings("unchecked")
public class NSCFactory /* NativeServiceClientFactory */ {

    /**
     * 接口实现缓存 这里是给本地应用程序使用（在不支持JavaEE7.0的情况下使用）
     */
    private static final Map<String, Object> clientImpls = new HashMap<>();

    /**
     * 获取接口实现
     * 
     * @param iface
     * @return
     */
    public static <T> T get(Class<T> iface, String alternative) {
        return (T) clientImpls.get(getClientImplKey(iface, alternative));
    }

    /**
     * 清空
     */
    public static void destory() {
        clientImpls.clear();
    }

    // --------------------------------------------------------------------------------------------------------//

    /**
     * 构建实体远程访问代理对象
     * 
     * @param clazzes
     */
    public static void build(Class<? extends ApiActivator>... clazzes) {
        build(false, clazzes);
    }

    /**
     * 构建实体远程访问代理对象
     * 
     * @param clazzes
     */
    public static void build(boolean debug, Class<? extends ApiActivator>... clazzes) {
        if (!clientImpls.isEmpty()) {
            clientImpls.clear();
        } // 情况原有系统数据

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Index index = createIndexer(loader, clazzes);
        ClassPool ctPool = ClassPool.getDefault();
        // 前期 clientImpls 中都是ApiActivator对象
        for (Object activatorObj : clientImpls.values().toArray()) {
            ApiActivator activator = (ApiActivator) activatorObj;
            // 由于默认的provider在本地访问中是失效的，所以在这里提供新的访问方式
            activator.setAdapter(ResteasyProviderFactory.class, getNativeProviderFactory());
            activator.init(); // 初始化
            try {// 创建远程接口实现
                Named named = activator.getClass().getAnnotation(Named.class);
                ClientServiceFactory.createImpl(activator, index, ctPool, (api, impl) -> {
                    Class<?> clazz = impl.toClass(loader, null);
                    if (debug) {
                        debugCtClass(impl);
                    }
                    Object apiObj = clazz.newInstance(); // 生成通信代理
                    if (apiObj instanceof ServiceClient) {
                        ServiceClient sc = (ServiceClient) apiObj;
                        sc.setActivator(activator); // 设置激活器
                        sc.init(); // 执行初始化
                    }
                    String key = getClientImplKey(api, named == null ? "" : named.value());
                    clientImpls.put(key, apiObj);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------//

    /**
     * 提供器
     * 
     * @return
     */
    public static ResteasyProviderFactory getNativeProviderFactory() {
        // create a new one
        ResteasyProviderFactory providerFactory = new LocalResteasyProviderFactory(ResteasyProviderFactory.newInstance());
        // ResteasyProviderFactory providerFactory = new
        // LocalResteasyProviderFactory(ResteasyProviderFactory.getInstance());
        RegisterBuiltin.register(providerFactory);
        providerFactory.registerProvider(JacksonJsonProvider.class, true); // 装载翻译器
        providerFactory.registerProvider(JacksonXMLProvider.class, true); // 装载翻译器
        return providerFactory;
    }

    /**
     * 本地环境，存放接口和实现的检索key
     * 
     * @param iface
     * @param alternative
     * @return
     */
    private static String getClientImplKey(Class<?> iface, String alternative) {
        return iface.getCanonicalName() + "_#" + alternative;
    }

    /**
     * 构建Index
     * 
     * @param loader
     * @param clazzes
     * @return
     */
    private static Index createIndexer(ClassLoader loader, Class<? extends ApiActivator>... clazzes) {
        Indexer indexer = new Indexer();
        try {
            Set<Class<?>> useClasses = new HashSet<>();
            for (Class<? extends ApiActivator> activatorClass : clazzes) {
                ApiActivator activator = activatorClass.newInstance();
                clientImpls.put(getClientImplKey(activatorClass, ""), activator); // 放入缓存
                for (Class<?> apiClass : activator.getClasses()) {
                    useClasses.add(apiClass);
                    for (Method method : apiClass.getMethods()) {
                        for (Class<?> paramType : method.getParameterTypes()) {
                            if (!paramType.isPrimitive()) {
                                useClasses.add(paramType);
                            }
                        }
                    }
                }
            }
            for (Class<?> clazz : useClasses) {
                InputStream is = loader.getResourceAsStream(clazz.getName().replace('.', '/') + ".class");
                indexer.index(is);
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return indexer.complete();
    }

    // -------------------------------------------------TEST-----------------------------------------------------//
    private static void debugCtClass(CtClass ctClass) {
        try {
            ctClass.writeFile("D:/classes");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
