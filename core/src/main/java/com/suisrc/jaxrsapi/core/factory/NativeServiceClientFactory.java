package com.suisrc.jaxrsapi.core.factory;

import java.util.ArrayList;
import java.util.List;

import org.jboss.jandex.IndexView;
import org.jboss.resteasy.client.jaxrs.internal.LocalResteasyProviderFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider;
import com.suisrc.core.Global;
import com.suisrc.jaxrsapi.core.ApiActivator;
import com.suisrc.jaxrsapi.core.JaxrsConsts;
import com.suisrc.jaxrsapi.core.ServiceClient;

/**
 * 执行本地处理 系统中静态存放远程访问实体
 * 
 * 该对象应对系统中没有web容器和自动加载机制的系统中使用
 * 
 * @author Y13
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class NativeServiceClientFactory {

    /**
     * 构建实体远程访问代理对象
     * 
     * @param clazzes
     */
    public static void initApiActivator(ApiActivator... activators) {
        for (ApiActivator activator : activators) {
            activator.setAdapter((String)null, ResteasyProviderFactory.class, getNativeProviderFactory());
            activator.doPostConstruct();
        }
    }

    /**
     * 构建实体远程访问代理对象
     * 生成class代码
     * @param clazzes
     */
    public static List<ApiActivator> buildClass(String target, String classKey, boolean newInstance, 
            Class<? extends ApiActivator>... clazzes) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        List<ApiActivator> activators = new ArrayList<>();
        IndexView index = ClientServiceFactory.createIndexer(activators, loader, clazzes);
        
        // 前期 clientImpls 中都是ApiActivator对象
        for (ApiActivator activator : activators) {
            // 由于默认的provider在本地访问中是失效的，所以在这里提供新的访问方式
            if (newInstance) {
                activator.setAdapter((String)null, ResteasyProviderFactory.class, getNativeProviderFactory());
                activator.doPostConstruct(); // 初始化
            }
            try {// 创建远程接口实现
                ClientServiceFactory.createImpl(activator, index, (api, impl) -> {
                    try {
                        if (target != null) {
                            impl.writeFile(target);
                        }
                        Class<?> clazz = impl.toClass(loader, null);
                        if (newInstance) {
                            Object apiObj = clazz.newInstance(); // 生成通信代理
                            if (apiObj instanceof ServiceClient) {
                                ServiceClient sc = (ServiceClient) apiObj;
                                sc.setActivator(activator); // 设置激活器 执行初始化
                            }
                            // 注册接口的实现
                            activator.registerApi(JaxrsConsts.RESTFUL_API_IMPL, (Class) api, apiObj);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, classKey, false); //该接口会回调注册接口，所以不用构建实现索引类
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        return activators;
    }
    /**
     * 构建实体远程访问代理对象
     * 
     * @param clazzes
     */
    public static void buildFile(String target, String classKey, Class<? extends ApiActivator>... clazzes) {
        buildFile(target, classKey, true, clazzes);
    }

    /**
     * 构建实体远程访问代理对象
     * 
     * @param clazzes
     */
    public static void buildFile(String target, String classKey, boolean init, Class<? extends ApiActivator>... clazzes) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        List<ApiActivator> activators = new ArrayList<>();
        IndexView index = ClientServiceFactory.createIndexer(activators, loader, clazzes);
        
        // 前期 clientImpls 中都是ApiActivator对象
        for (ApiActivator activator : activators) {
            if (init) {
                // 由于默认的provider在本地访问中是失效的，所以在这里提供新的访问方式
                activator.setAdapter((String)null, ResteasyProviderFactory.class, getNativeProviderFactory());
                activator.doPostConstruct(); // 初始化
            }
            try {
                // 创建远程接口实现
                // 代码中是无法注入接口的实现的
                ClientServiceFactory.createImpl(activator, index, target, classKey, true);
            } catch (Exception e) {
                throw new RuntimeException(e);
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
        RegisterBuiltin.register(providerFactory);
        providerFactory.registerProvider(JacksonJsonProvider.class, true); // 装载翻译器
        providerFactory.registerProvider(JacksonXMLProvider.class, true); // 装载翻译器
        return providerFactory;
    }

    /**
     * 构建调用代码
     * @param args
     */
    public static void buildSources(String name, Class<? extends ApiActivator> activator) {
        String path = ClassLoader.getSystemResource("").getPath();
        path = path.substring(1, path.length() - "target/test-classes/".length()) + "src/main/java";
        buildFile(path, name, false, activator);
        Global.getLogger().info("Build Completed!");
    }
}
