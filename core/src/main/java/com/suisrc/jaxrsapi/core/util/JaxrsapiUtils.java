package com.suisrc.jaxrsapi.core.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import com.suisrc.jaxrsapi.core.annotation.RemoteApi;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * 
 * @author Y13
 *
 */
public class JaxrsapiUtils {

    /**
     * 获取一个包内容中的所有Remote Restful Api接口 这里默认使用了RemoteApi接口
     * 
     * @param packageName
     * @return
     */
    public static Set<Class<?>> getRemoteApiClasses(Class<? extends Annotation> anno, boolean honorInherited, String... pkgs) {
        ConfigurationBuilder config = new ConfigurationBuilder();
        config.forPackages(pkgs);
        config.setScanners(new TypeAnnotationsScanner(), new SubTypesScanner());
        Reflections reflections = new Reflections(config);
        return reflections.getTypesAnnotatedWith(anno != null ? anno : RemoteApi.class, honorInherited);
    }

    /**
     * 获取一个接口的所有实现
     * 
     * @param packageName
     * @return
     */
    public static <T> Set<Class<? extends T>> getSubclasses(Class<T> iface, String... pkgs) {
        ConfigurationBuilder config = new ConfigurationBuilder();
        config.forPackages(pkgs);
        config.setScanners(new SubTypesScanner());
        Reflections reflections = new Reflections(config);
        return reflections.getSubTypesOf(iface);
    }

    /**
     * 获取CtClass,如果找不到，尝试增加后，再次查找 解决发布到发布到Jboss, Tomcat上，发生javassist.NotFoundException异常
     * 
     * @param pool
     * @param clazz
     * @return
     * @throws NotFoundException
     */
    public static synchronized CtClass getCtClass(ClassPool pool, Class<?> clazz) throws NotFoundException {
        try {
            return pool.get(clazz.getName());
        } catch (NotFoundException e) {
            ClassClassPath classPath = new ClassClassPath(clazz);
            pool.insertClassPath(classPath);
            return pool.get(clazz.getName());
        }
    }

    /**
     * 获取CtClass,如果找不到，尝试增加后，再次查找 解决发布到发布到Jboss, Tomcat上，发生javassist.NotFoundException异常
     * 
     * @param pool
     * @param clazz
     * @return
     * @throws NotFoundException
     */
    public static synchronized CtClass getCtClass(ClassPool pool, String className)
            throws NotFoundException, ClassNotFoundException {
        try {
            return pool.get(className);
        } catch (NotFoundException e) {
            ClassClassPath classPath = new ClassClassPath(Class.forName(className));
            pool.insertClassPath(classPath);
            return pool.get(className);
        }
    }

    /**
     * 获取消息的内容
     * 
     * @param inputStream
     * @return
     */
    public static String getContent(InputStream inputStream) {
        try {
            InputStreamReader isr = new InputStreamReader(inputStream, "UTF-8");
            char[] bufs = new char[1024];
            int len = 0;
            StringBuilder sbir = new StringBuilder();
            while ((len = isr.read(bufs)) > 0) {
                sbir.append(bufs, 0, len);
            }
            return sbir.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 执行无法明显调用的方法 主要用于兼容旧系统
     * 
     * @param clazz
     * @param owner
     * @param methodName
     * @param parameterTypes
     * @param parameters
     * @return
     */
    public static Object invoke(Class<?> ownerType, Object owner, String methodName, Class<?>[] parameterTypes,
            Object[] parameters) {
        try {
            Method method = (ownerType != null ? ownerType : owner.getClass()).getMethod(methodName, parameterTypes);
            return method.invoke(owner, parameters);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 执行无法明显调用的方法 主要用于兼容旧系统
     * 
     * @param clazz
     * @param owner
     * @param methodName
     * @param parameterTypes
     * @param parameters
     * @return
     */
    public static Object invokeDecared(Class<?> ownerType, Object owner, String methodName, Class<?>[] parameterTypes,
            Object[] parameters) {
        try {
            Method method = (ownerType != null ? ownerType : owner.getClass()).getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            Object result = method.invoke(owner, parameters);
            method.setAccessible(false);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 执行多层Get方法获取数据
     * 
     * @param owner
     * @param methodName
     * @return
     */
    public static Object invokeGet(Object owner, String... methods) {
        try {
            Object result = owner;
            for (String name : methods) {
                Method method = result.getClass().getMethod(name);
                result = method.invoke(result);
                if (result == null) {
                    return null;
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取私有和保护的对象属性
     * 
     * @param clazz
     * @param owner
     * @param methodName
     * @return
     */
    public static Object invokeGetField(Class<?> ownerType, Object owner, String fieldName) {
        try {
            Field field = ownerType.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object result = field.get(owner);
            field.setAccessible(false);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取Bean的结构
     * 
     * @param result
     * @return
     */
    public static String print(Object result) {
        try {
            StringBuilder sbir = new StringBuilder();
            Method[] methods = result.getClass().getMethods();
            for (Method method : methods) {
                if (!method.getName().startsWith("get")) {
                    continue;
                }

                String name = method.getName().substring("get".length());
                sbir.append(name).append(" : ");
                sbir.append(method.invoke(result)).append('\n');
            }
            return sbir.toString();
        } catch (Exception e) {
            return "error";
        }
    }
    
    /**
     * 创建一个类型实体
     * @param clazz
     * @return
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
