package com.suisrc.jaxrsapi.core;

import java.io.File;
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
 * @author Y13
 */
public abstract class AccessTokenActivator extends AbstractActivator {
    
    /**
     * app id
     */
    protected String appId;

    /**
     * app secret
     */
    protected String appSecret;
    
    /**
     * 远程访问执行到地址
     */
    protected String baseUrl;

    /**
     * token 强原子操作
     */
    protected TokenReference token = null;
    
    /**
     * token到调度器
     */
    protected Scheduler tokenScheduler = null;
    
    /**
     * 服务是否在重启中
     */
    protected RefVol<Boolean> resetScheduler = new RefVol<>(false);

    // ----------------------------------------------------------------ZERO ApiActivator
    
    /**
     * 构造后被系统调用 进行内容初始化
     */
    public void postConstruct() {
        appId = System.getProperty(getAppIdKey());
        appSecret = System.getProperty(getAppSecretKey());
        baseUrl = System.getProperty(getBaseUrlKey());
        super.postConstruct();
        token = initToken();
    }
    
    // ----------------------------------------------------------------ZERO 通用接口属性索引
    /**
     * 获取key索引
     * @return
     */
    protected abstract String getAppIdKey();
    /**
     * 获取密钥索引
     * @return
     */
    protected abstract String getAppSecretKey();
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
    protected abstract Token getTokenByRemote();

    /**
     * 获取令牌索引
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
    protected void restartUpdateTokenScheduler() {
        if (tokenScheduler != null) {
            // 重启
            tokenScheduler.restart();
        } else {
            tokenScheduler = new CustomDelayScheduler("[" + getAppId() + "]更新令牌服务", s -> {
                long delay = token.get().getExpiresIn() - getTokenAdvanceIn();
                if (delay < 0) {
                    // 防止死锁，返回保留1s时间
                    return 1L;
                }
                return delay;
            }, this::newToken);
            // 启动更新服务器
            tokenScheduler.start();
        }
    }
    
    // ----------------------------------------------------------------ZERO Adapter
    /**
     * 获取系统中常用的数据配置 返回系统中常量数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(String key) {
        if (key.equals(getTokenKey())) {
            return (T)getToken();
        }
        if (key.equals(getBaseUrlKey())) {
            return (T)getBaseUrl();
        }
        if (key.equals(getAppIdKey())) {
            return (T)getAppId();
        }
        if (key.equals(getAppSecretKey())) {
            return (T)getAppSecret();
        }
        return super.getAdapter(key);
    }

    //--------------------------------------------------ZERO Config
    
    public String getBaseUrl() {
        return baseUrl;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    //--------------------------------------------------ZERO AccessToken
    /**
     * 获取access token
     */
    public String getToken() {
        internalCheckToken();
        return token.get().getAccessToken();
    }

    /**
     * 清空令牌
     */
    public void clearToken(boolean stopService) {
        token.set(new Token()); // 清空access token
        if (token != null && stopService) {
            tokenScheduler.stop(); // 关闭更新服务
            tokenScheduler = null; // 删除服务
        }
    }

    /**
     * 检测token是否可用
     */
    protected void internalCheckToken() {
        if (isAutoUpdateToken()) {
            // 自动更新
            try {
                boolean check = true;
                int count = 5; // 最多检测次数，如果检测失败，重启更新服务
                while (check && count-- > 0) {
                    TokenStatus status = getAccessTokenStatus();
                    switch (status) {
                        case NONE:
                        case EXPIRED:// 同步刷新
                            synchronized (resetScheduler) {
                                if (!resetScheduler.get()) {
                                    // 标记重启服务更新
                                    resetScheduler.set(true);
                                    // 启动更新服务
                                    restartUpdateTokenScheduler();
                                    // 等待更新服务启动完成
                                    tokenScheduler.awaitRunning();
                                    System.out.println("令牌更新服务异常，已经重启令牌更新服务。");
                                }
                            }
                            // 进程休眠1s,防止线程间恶意竞争
                            try { Thread.sleep(1000); } catch (InterruptedException e) {}
                            break;
                        case WILL_EXPIRE:
                            if (tokenScheduler == null || !tokenScheduler.isRunning()) {
                                // 更新服务异常，并没有在运行， 重启服务
                                restartUpdateTokenScheduler();
                                System.out.println("令牌更新服务异常，已经通知重启。");
                            }
                        case VALID:
                            check = false; // 检查通过，token可用使用
                        default: break;
                    }
                }
            } finally {
                if (resetScheduler.get()) {
                    resetScheduler.set(false);
                }
            }
        } else {
            // 非自动更新的状况
            TokenStatus status = getAccessTokenStatus();
            switch (status) {
                case NONE:
                case EXPIRED:// 同步刷新
                    newToken();
                    break;
                case WILL_EXPIRE:// 异步刷新
                    // 服务器已经有进行正在同步，跳过
                    if (!token.getSyncLock().get()) { 
                        CompletableFuture.runAsync(this::newToken, executor);
                    }
                case VALID:
                default: break; // access token 正常使用
            }
        }
    }
    
    /**
     * 初始化构造AccessToken
     */
    protected TokenReference initToken() {
        if (getAppId() == null || getAppSecret() == null) {
            throw new RuntimeException("'AppId' is null or 'AppSecret' is null.");
        }
        // 从拓扑网络中获取token
        TokenReference token = getTokenByTopology();
        if (token != null) {
            return token;
        }
        token = new TokenReference();
        // 读取系统文件中的access token
        // 拓扑网络中的token和临时系统中的token是不一样的
        // 请注意，该部分大部分用于调试，不用频繁访问token中控服务器
        Token tkn = (Token) readTempObject();
        if (tkn == null) {
            // 初始化一个无效凭证
            tkn = new Token();
        }
        token.set(tkn);
        token = putTokenByTopology(token);
        return token;
    }

    /**
     * 更新access token
     */
    protected synchronized void newToken() {
        if (hasUpdateTokenByTopology()) {
            // 拓扑网络环境中，有其他节点正在修改token
            return;
        }
        if (getAccessTokenStatus() == TokenStatus.VALID) {
            return; // 已经被其他线程同步过
        }
        try {
            token.getSyncLock().set(true); // 同步标识打开
            lockUpdateTokenByTopology();
            Token tkn = getTokenByRemote();
            if (tkn != null) {
                writeTempObject(tkn);
                token.set(tkn);
            }
        } finally {
            token.getSyncLock().set(false); // 同步表示关闭
            unlockUpdateTokenByTopology();
        }
    }
    
    /**
     * 获取令牌的状态
     * @return
     */
    protected TokenStatus getAccessTokenStatus() {
        return token.get().getStatus(getTokenAdvanceIn());
    }
    
    //---------------------------------------------------------------------集群同步

    /**
     * 更新拓扑中的token
     * @param key
     * @param accessToken
     * @return
     */
    protected TokenReference putTokenByTopology(TokenReference token) {
        return token;
    }

    /**
     * 获取拓扑中的token
     * @param key
     * @return
     */
    protected TokenReference getTokenByTopology() {
        return null;
    }
    
    /**
     * 检测拓扑网络中是否有人正在修改token
     * @return
     */
    protected boolean hasUpdateTokenByTopology() {
        return false;
    }
    
    /**
     * 标记拓扑网络，当前环境正在修改token
     * 
     * 当执行set操作后，应该立即解锁拓扑网络中的标记
     * @return ture 标记成功， false 标记失败，当前有其他环境正在修改token
     */
    protected boolean lockUpdateTokenByTopology() {
        return true;
    }
   
    /**
     * 解锁拓扑网络中对token更新的锁定
     * @return
     */
    protected void unlockUpdateTokenByTopology() {
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
     * 把access token写入文件中，避免测试中频繁调用
     * 
     * @param token
     */
    protected void writeTempObject(Object obj) {
        if (!DEBUG) {
            return;
        }
        String filename = getTempFileName();
        if (filename == null || filename.isEmpty()) {
            return;
        }
        FileUtils.writeObject(filename, obj);
    }

    /**
     * 从文件中读入access token
     * 
     * @return
     */
    protected Object readTempObject() {
        if (!DEBUG) {
            return null;
        }
        String filename = getTempFileName();
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        File file = new File(filename);
        if (!file.exists()) {
            return null;
        }
        return FileUtils.writeObject(filename);
    }
}
