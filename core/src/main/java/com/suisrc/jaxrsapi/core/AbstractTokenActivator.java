package com.suisrc.jaxrsapi.core;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.suisrc.core.reference.RefVol;
import com.suisrc.core.scheduler.CustomDelayScheduler;
import com.suisrc.core.scheduler.Scheduler;
import com.suisrc.core.utils.FileUtils;
import com.suisrc.jaxrsapi.core.token.Token;
import com.suisrc.jaxrsapi.core.token.TokenReference;
import com.suisrc.jaxrsapi.core.token.TokenStatus;

/**
 * 程序入口配置抽象
 * 
 * 多令牌模式
 * 
 * 在一些情况下，系统希望有多个令牌控制系统
 * 
 * 在业务开发过程中，发现对于微信企业号服务时候，其希望激活器具有同时控制多个企业号的功能，
 * 同时也希望这些企业号也可以时候扩展，这样我们希望有一个同时可以控制多个企业号的截获器。
 * 
 * @author Y13
 */
public abstract class AbstractTokenActivator extends AbstractActivator {
    
    /**
     * 一个令牌的原子类
     * 其中包含令牌的原子对象，令牌的调度器，令牌调度器重启标识
     * 
     * 该类不提供赋值方法,如果需要新建，可以通过getTokenAtom方法获取
     */
    public static class TokenAtom implements Serializable {
        private static final long serialVersionUID = 2068405918828186109L;
        /**
         * token 关键字
         */
        private String tokenKey = null;
        /**
         * token 强原子操作
         */
        private TokenReference token = null;
        /**
         * token到调度器
         */
        private Scheduler tokenScheduler = null;
        /**
         * 服务是否在重启中
         */
        private RefVol<Boolean> resetScheduler = new RefVol<>(false);
        
        /**
         * 禁止其他地方构造方法调用
         */
        protected TokenAtom() {}
        
        public String getTokenKey() {
            return tokenKey;
        }
        public TokenReference getToken() {
            return token;
        }
        public Scheduler getTokenScheduler() {
            return tokenScheduler;
        }
        public RefVol<Boolean> getResetScheduler() {
            return resetScheduler;
        }
    }
    
    /**
     * 远程访问执行到地址
     */
    protected String baseUrl;

    // ----------------------------------------------------------------ZERO ApiActivator
    
    /**
     * 构造后被系统调用 进行内容初始化
     */
    @Override
    public void doPostConstruct() {
        baseUrl = System.getProperty(getBaseUrlKey(), getDefaultBaseUrl());
        super.doPostConstruct();
    }
    
    /**
     * 如果配置文件没有，返回的默认URL
     * @return
     */
    protected String getDefaultBaseUrl() {
        return null;
    }

    // ----------------------------------------------------------------ZERO 通用接口属性索引    
    /**
     * 获取应用的名称
     * 
     * 该名称是启动的应用服务器名称，与绑定的第三方账户信息内容无关
     * @return
     */
    public abstract String getAppName();

    /**
     * 获取基础路径索引
     * @return
     */
    protected abstract String getBaseUrlKey();
    
    /**
     * 获取 weixin access token, 
     * 不需要保证线程安全，框架中已经控制了线程安全
     * 获取新的token对象
     * 
     * @return
     */
    protected abstract Token getTokenByRemote(String tokenKey);

    /**
     * 查询令牌元
     * @param tokenKey
     * @return
     */
    public abstract TokenAtom findTokenAtom(String tokenKey);
    
    /**
     * 保存令牌元
     * 
     * 这里带有返回值，是为令牌元增加代理做扩展
     * @param tokenKey
     * @param tokenAtom
     * @return
     */
    public abstract TokenAtom saveTokenAtom(String tokenKey, TokenAtom tokenAtom);

    /**
     * 获取令牌索引
     * 
     * 令牌索引需要增加调用的应用关键字才是一个完整的令牌
     * @return
     */
    protected String getTokenKey() {
        return "TOKEN";
    }
    
    /**
     * 是否自动更新token
     * 
     * 如果配置为ture, 更新token到更新器会在第一个获取token后激活。
     * 
     * 默认不使用中控服务
     * 
     * @return
     */
    protected boolean isAutoUpdateToken() {
        return false;
    }

    /**
     * 获取令牌的提前更新时间（注意，是判定提前时间）
     * 
     * 单位: 秒
     * 
     * 默认值为300s, 即5分钟
     * 
     * @return
     */
    protected long getTokenAdvanceIn() {
        return 300;
    }
    /**
     * 获取更新token到线程池
     * @return
     */
    protected void restartUpdateTokenScheduler(String tokenKey) {
        TokenAtom tokenAtom = getTokenAtom(tokenKey, false);
        if (tokenAtom == null) {
            return; // 无法执行任何操作，执行服务中token的自动更新前提是必须有执行的内容
        }
        if (tokenAtom.getTokenScheduler() != null) {
            // 重启
            tokenAtom.getTokenScheduler().restart();
        } else {
            String name = "[" + getAppName() + ":" + tokenAtom.getTokenKey() + "]更新令牌服务";
            tokenAtom.tokenScheduler = new CustomDelayScheduler(name, s -> {
                long delay = tokenAtom.getToken().get().getExpiresIn() - getTokenAdvanceIn();
                if (delay < 0) {
                    // 防止死锁，返回保留1s时间
                    return 1L;
                }
                return delay;
            }, () -> this.processNewToken(tokenAtom));
            // 启动更新服务器
            tokenAtom.getTokenScheduler().start();
        }
    }

    // ----------------------------------------------------------------ZERO Adapter
    /**
     * 获取系统中常用的数据配置 返回系统中常量数据
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(String key, Class<T> type) {
        if (type == String.class && key.equals(getTokenKey())) {
            return (T)getToken();
        }
        return super.getAdapter(key, type);
    }

    //--------------------------------------------------ZERO Config

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    //--------------------------------------------------ZERO AccessToken
    
    /**
     * 获取token
     */
    public String getToken(String tokenKey) {
        TokenAtom ta = getInternalCheckToken(tokenKey);
        return ta.getToken().get().getAccessToken();
    }

    /**
     * 清空令牌
     */
    public void clearToken(String tokenKey, boolean stopService) {
        TokenAtom tokenAtom = getTokenAtom(tokenKey, false);
        if (tokenAtom == null) {
            return;
        }
        tokenAtom.getToken().set(new Token()); // 清空access token
        if (tokenAtom.getTokenScheduler() != null && stopService) {
            tokenAtom.getTokenScheduler().stop(); // 关闭更新服务
            tokenAtom.tokenScheduler = null; // 删除服务
        }
    }
    
    /**
     * 
     * @param tokenKey
     * @param ifnew
     * @return
     */
    protected TokenAtom getTokenAtom(String tokenKey, boolean ifnew) {
        TokenAtom tokenAtom = findTokenAtom(tokenKey);
        if (tokenAtom == null && ifnew) {
            tokenAtom = saveTokenAtom(tokenKey, new TokenAtom());
            if (tokenAtom.tokenKey == null) {
                tokenAtom.tokenKey = tokenKey;
            }
        }
        return tokenAtom;
    }

    /**
     * 初始化构造AccessToken
     * 
     * 初始化该内容时候，系统记性同步操作
     */
    protected synchronized TokenAtom initTokenAtom(String tokenKey) {
        TokenAtom tokenAtom = getTokenAtom(tokenKey, true);
        if (tokenAtom.getToken() != null) {
            // token是新的，这里进行初始内容
            return tokenAtom;
        }
        // 从拓扑网络中获取token
        TokenReference token = getTokenByTopology(tokenKey);
        if (token != null) {
            tokenAtom.token = token;
            return tokenAtom;
        }
        token = new TokenReference();
        // 读取系统文件中的access token
        // 拓扑网络中的token和临时系统中的token是不一样的
        // 请注意，该部分大部分用于调试，不用频繁访问token中控服务器
        Token tkn = (Token) readTempObject(tokenKey);
        if (tkn == null) {
            // 初始化一个无效凭证
            tkn = new Token();
        }
        token.set(tkn);
        token = putTokenByTopology(tokenKey, token);
        tokenAtom.token = token;
        return tokenAtom;
    }

    /**
     * 检测token是否可用
     */
    protected TokenAtom getInternalCheckToken(String tokenKey) {
        TokenAtom tokenAtom = getTokenAtom(tokenKey, false);
        if (tokenAtom == null) {
            // 初始化一个token, 必须成功否则抛出异常
            tokenAtom = initTokenAtom(tokenKey);
        }
        if (isAutoUpdateToken()) {
            // 自动更新
            checkTokenByDriving(tokenAtom);
        } else {
            // 被动更新
            checkTokenByPassive(tokenAtom);
        }
        return tokenAtom;
    }

    /**
     * 对访问使用的令牌进行被动生命周期检测
     * @param tokenAtom
     */
    protected void checkTokenByPassive(TokenAtom tokenAtom) {
        TokenStatus status = getAccessTokenStatus(tokenAtom);
        switch (status) {
            case NONE:
            case EXPIRED:// 同步刷新
                processNewToken(tokenAtom);
                break;
            case WILL_EXPIRE:// 异步刷新
                // 服务器已经有进行正在同步，跳过
                if (!tokenAtom.getToken().getSyncLock().get()) {
                    TokenAtom ta = tokenAtom;
                    CompletableFuture.runAsync(() -> this.processNewToken(ta), executor);
                }
            case VALID:
            default: break; // access token 正常使用
        }
    }

    /**
     * 对访问使用的令牌进行主动生命周期检测
     * @param tokenAtom
     */
    protected void checkTokenByDriving(TokenAtom tokenAtom) {
        try {
            boolean check = true;
            int count = 5; // 最多检测次数，如果检测失败，重启更新服务
            while (check && count-- > 0) {
                TokenStatus status = getAccessTokenStatus(tokenAtom);
                switch (status) {
                    case NONE:
                    case EXPIRED:// 同步刷新
                        synchronized (tokenAtom.getResetScheduler()) {
                            if (!tokenAtom.getResetScheduler().get()) {
                                // 标记重启服务更新
                                tokenAtom.getResetScheduler().set(true);
                                // 启动更新服务
                                restartUpdateTokenScheduler(tokenAtom.getTokenKey());
                                // 等待更新服务启动完成
                                tokenAtom.getTokenScheduler().awaitRunning();
                                logger.warning(tokenAtom.getTokenKey() + "令牌更新服务异常，已经重启令牌更新服务。");
                            }
                        }
                        // 进程休眠1s,防止线程间恶意竞争
                        try { Thread.sleep(1000); } catch (InterruptedException e) {}
                        break;
                    case WILL_EXPIRE:
                        if (tokenAtom.getTokenScheduler() == null || !tokenAtom.getTokenScheduler().isRunning()) {
                            // 更新服务异常，并没有在运行， 重启服务
                            restartUpdateTokenScheduler(tokenAtom.getTokenKey());
                            logger.warning(tokenAtom.getTokenKey() + "令牌更新服务异常，已经通知重启。");
                        }
                    case VALID:
                        check = false; // 检查通过，token可用使用
                    default: break;
                }
            }
        } finally {
            // 强制刷新令牌同步 标识
            if (tokenAtom.getResetScheduler().get()) {
                tokenAtom.getResetScheduler().set(false);
            }
        }
    }

    /**
     * 更新access token
     */
    protected void processNewToken(TokenAtom tokenAtom) {
        if (hasUpdateTokenByTopology(tokenAtom.getTokenKey())) {
            // 拓扑网络环境中，有其他节点正在修改token
            return;
        }
        if (getAccessTokenStatus(tokenAtom) == TokenStatus.VALID) {
            return; // 已经被其他线程同步过
        }
        try {
            tokenAtom.getToken().getSyncLock().set(true); // 同步标识打开
            lockUpdateTokenByTopology(tokenAtom.getTokenKey());
            Token tkn = getTokenByRemote(tokenAtom.getTokenKey());
            if (tkn != null) {
                writeTempObject(tokenAtom.getTokenKey(), tkn);
                tokenAtom.getToken().set(tkn);
            }
        } finally {
            tokenAtom.getToken().getSyncLock().set(false); // 同步表示关闭
            unlockUpdateTokenByTopology(tokenAtom.getTokenKey());
        }
    }
    
    /**
     * 获取令牌的状态
     * @return
     */
    protected TokenStatus getAccessTokenStatus(TokenAtom tokenAtom) {
        return tokenAtom.getToken().get().getStatus(getTokenAdvanceIn());
    }
    
    //---------------------------------------------------------------------集群同步

    /**
     * 更新拓扑中的token
     * @param key
     * @param accessToken
     * @return
     */
    protected TokenReference putTokenByTopology(String tokenKey, TokenReference token) {
        return token;
    }

    /**
     * 获取拓扑中的token
     * @param key
     * @return
     */
    protected TokenReference getTokenByTopology(String tokenKey) {
        return null;
    }

    /**
     * 获取拓扑中的token
     * @param key
     * @return
     */
    protected List<TokenReference> getTokensByTopology() {
        return null;
    }
    
    /**
     * 检测拓扑网络中是否有人正在修改token
     * @return
     */
    protected boolean hasUpdateTokenByTopology(String tokenKey) {
        return false;
    }
    
    /**
     * 标记拓扑网络，当前环境正在修改token
     * 
     * 当执行set操作后，应该立即解锁拓扑网络中的标记
     * @return ture 标记成功， false 标记失败，当前有其他环境正在修改token
     */
    protected boolean lockUpdateTokenByTopology(String tokenKey) {
        return true;
    }
   
    /**
     * 解锁拓扑网络中对token更新的锁定
     * @return
     */
    protected void unlockUpdateTokenByTopology(String tokenKey) {
    }

    //---------------------------------------------------------------------借助系统默认缓存处理内容
    /**
     * 通过辅助的缓存内容获取token关键字
     * 
     * 以下内容是对原有内容的辅助增强
     * 也是对以前单模态下的访问令牌控制器的兼容
     * 前期必须重写getTokenKeyByCache
     */
    protected String getTokenKeyByCache() {
        return null;
    }
    
    /**
     * 获取access token
     */
    public String getToken() {
        String tokenKey = getTokenKeyByCache();
        if (tokenKey == null) {
            return null;
        }
        return getToken(tokenKey);
    }

    /**
     * 清空令牌
     */
    public void clearToken(boolean stopService) {
        String tokenKey = getTokenKeyByCache();
        if (tokenKey == null) {
            return;
        }
        clearToken(tokenKey, stopService);
    }

    //---------------------------------------------------------------------临时文件保存
    /**
     * 临时缓存文件的名字
     * 
     * @return
     */
    protected String getTempFileName() {
        return null;
    }
    
    /**
     * 修正文件名称
     */
    protected String getTempFileNameByKey(String filename, String key) {
        if (key != null && !key.isEmpty()) {
            int offset = filename.lastIndexOf('.');
            if (offset > 0) {
                filename = filename.substring(0, offset) + key + filename.substring(offset);
            } else {
                filename += "." + key;
            }
        }
        return filename;
    }

    /**
     * 把access token写入文件中，避免测试中频繁调用
     * 
     * @param token
     */
    protected void writeTempObject(String key, Object obj) {
        if (!DEBUG) {
            return;
        }
        String filename = getTempFileName();
        if (filename == null || filename.isEmpty()) {
            return;
        }
        filename = getTempFileNameByKey(filename, key);
        FileUtils.writeObject(filename, obj);
    }

    /**
     * 从文件中读入access token
     * 
     * @return
     */
    protected Object readTempObject(String key) {
        if (!DEBUG) {
            return null;
        }
        String filename = getTempFileName();
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        filename = getTempFileNameByKey(filename, key);
        File file = new File(filename);
        if (!file.exists()) {
            return null;
        }
        return FileUtils.readObject(filename);
    }
}
