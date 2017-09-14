package com.suisrc.jaxrsapi.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * 全局属性，操作的时候访问, 所有的缓存为单服务器缓存，不进行集群同步
 * 
 * 该内容中的属性都采用的是静态属性，所以在使用的时候，需要注意清除，防止两份自动资源
 * 
 * 再次强调，该类中的方法，全局静态变量缓存尽可能的少用。
 * 
 * @author Y13
 *
 */
public class Global {

    /** 流水线上的缓冲，是一个性格孤僻的人,需要自己控制注意清空 */
    private static final ThreadLocal<Map<Object, Object>> loner_cache = new ThreadLocal<>();

    /**
     * 1.本地缓存,不与集群同步,存储本地特有数据内容 
     * 2.数据的无状态性质 
     * 3.只有在重启系统的时候才更新 所以使用全局本地缓存的时候需要提别注意以上几点
     */
    private static final Map<Object, Object> data_cache = new ConcurrentHashMap<>();

    /**
     * 线程池对象, 该线程池处于一直运行状态 不要申请过大的空间和数量，最好维持在3个左右
     */
    private static ExecutorService executor_service = null;

    /** ----------------------分割线 TODO 线程缓存 相关联内容 */

    /**
     * 获取线程缓存
     */
    @SuppressWarnings("unchecked")
    public static <T> T getThreadCache(Object key) {
        Map<Object, Object> map = loner_cache.get();
        return map == null ? null : (T) map.get(key);
    }

    /**
     * 增加线程缓存，由于线程缓存就有单线程性质，所以我们可以认为是线程安全的
     */
    public static void putThreadCache(Object key, Object value) {
        Map<Object, Object> map = loner_cache.get();
        if (map == null) {
            loner_cache.set(map = new HashMap<>());
        }
        map.put(key, value);
    }

    /**
     * 移除移除线程缓存
     */
    public static void removeThreadCache(Object key) {
        Map<Object, Object> map = loner_cache.get();
        if (map != null) {
            map.remove(key);
        }
    }

    /**
     * 移除移除线程缓存
     */
    public static void removeThreadCache() {
        loner_cache.remove();
    }

    /** ----------------------分割线 TODO 全局缓存 */

    /**
     * 把数据放入全局缓存中
     * 
     * @param key
     * @param data
     */
    public static void putCache(Object key, Object data) {
        data_cache.put(key, data);
    }

    /**
     * 从全局缓存中获取数据
     * 
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getCache(Object key) {
        return (T) data_cache.get(key);
    }

    /**
     * 情况全局缓存
     * 
     * @param key
     */
    public static void clearCache(Object key) {
        data_cache.clear();
    }

    /**
     * 移除全局缓存中的指定数据
     * 
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T removeCache(Object key) {
        return (T) data_cache.remove(key);
    }

    /** ----------------------分割线 TODO 线程池 */

    /**
     * 创建全局线程池
     * 
     * @return
     */
    public static boolean initializeExecutor() {
        return initializeExecutor(3);
    }

    /**
     * 创建全局线程池
     * 
     * @param count
     * @return
     */
    public static boolean initializeExecutor(int count) {
        if (executor_service != null) {
            return false;
        }
        executor_service = Executors.newFixedThreadPool(count);
        return true;
    }

    /**
     * 提交任务，提交后就不再管理
     * 
     * @param task
     * @return
     */
    public static boolean submitRunnable(Runnable task) {
        if (executor_service == null) {
            return false;
        }
        return executor_service.submit(task) != null;
    }

    /**
     * 提交任务, 具有回调等待的特性
     * 
     * @param task
     * @return
     */
    public static <V> Future<V> submitCallable(Callable<V> task) {
        if (executor_service == null) {
            return null;
        }
        return executor_service.submit(task);
    }

    /**
     * 释放线程池
     */
    public static void destoryExecutor() {
        if (executor_service != null) {
            // 关闭线程池，即使有数据没有执行完成，也关闭
            // 不知道这里是否用shutdown更好
            executor_service.shutdownNow();
            executor_service = null;
        }
    }

    /**
     * JBOSS标准中的JavaEE线程池，暂时放弃JBoss的特性内容
     */
    // public static <U> void exec(Supplier<U> restMethod, Consumer<U> handler, Consumer<Throwable>
    // exceptionHandler) throws Exception {
    // chainableExec(restMethod, exceptionHandler).thenAccept(handler).exceptionally(t -> {
    // exceptionHandler.accept(t);
    // return null;
    // });
    // }
    //
    // public static <U> CompletableFuture<U> chainableExec(Supplier<U> restMethod,
    // Consumer<Throwable> exceptionHandler) throws Exception {
    // return CompletableFuture.supplyAsync(restMethod, executorService()).exceptionally(t -> {
    // exceptionHandler.accept(t);
    // return null;
    // });
    // }
    //
    // public static ManagedExecutorService executorService() throws Exception {
    // InitialContext ctx = new InitialContext();
    // return (ManagedExecutorService) ctx.lookup("java:jboss/ee/concurrency/executor/default");
    // }

    /** ----------------------分割线 TODO 对默认值进行限定 */

    @SuppressWarnings("unchecked")
    public static <T> T getValue(Function<String, T> handler, String key, T defaultValue, T... candidate) {
        T value = handler.apply(key);
        return value != null && (candidate.length == 0 || Arrays.asList(candidate).contains(value)) ? value : defaultValue;
    }

    /**
     * 需要保证key不为null
     * 
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValue(String key) {
        if (key.startsWith(Consts.PRE_THREAD)) {
            return Global.getThreadCache(key.substring(Consts.PRE_THREAD.length()));
        }
        if (key.startsWith(Consts.PRE_GLOBAL)) {
            return Global.getCache(key.substring(Consts.PRE_GLOBAL.length()));
        }
        if (key.startsWith(Consts.PRE_ENV11T)) {
            return (T) System.getenv(key.substring(Consts.PRE_ENV11T.length()));
        }
        if (key.startsWith(Consts.PRE_SYSTEM)) {
            return (T) System.getProperty(key.substring(Consts.PRE_SYSTEM.length()));
        }
        return null; // 没有找到
    }
}
