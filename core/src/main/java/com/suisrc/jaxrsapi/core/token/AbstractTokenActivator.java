package com.suisrc.jaxrsapi.core.token;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.suisrc.core.scheduler.CustomDelayScheduler;
import com.suisrc.jaxrsapi.core.AbstractActivator;

/**
 * <p> 程序入口配置抽象
 * 
 * <p> 多令牌模式
 * 
 * <p> 在一些情况下，系统希望有多个令牌控制系统
 * 
 * <p> 在业务开发过程中，发现对于微信企业号服务时候，其希望激活器具有同时控制多个企业号的功能， <p> 同时也希望这些企业号也可以时候扩展，这样我们希望有一个同时可以控制多个企业号的截获器。
 * 
 * @author Y13
 */
public abstract class AbstractTokenActivator extends AbstractActivator {

  /**
   * <p> 远程访问执行到地址
   */
  protected String baseUrl;

  /**
   * <p> 重新计算token的次数 <p> 仅作为统计的手段
   */
  private long tokenStatistics = 0;

  // ----------------------------------------------------------------ZERO ApiActivator

  /**
   * <p> 获取token获取的次数
   * 
   * @return
   */
  public long getTokenStatistics() {
    return tokenStatistics;
  }

  /**
   * <p> 构造后被系统调用 进行内容初始化
   */
  @Override
  public void doPostConstruct() {
    baseUrl = System.getProperty(getBaseUrlKey(), getDefaultBaseUrl());
    super.doPostConstruct();
  }

  /**
   * <p> 如果配置文件没有，返回的默认URL
   * 
   * @return
   */
  protected String getDefaultBaseUrl() {
    return null;
  }

  // ----------------------------------------------------------------ZERO 通用接口属性索引
  /**
   * <p> 获取应用的名称
   * 
   * <p> 该名称是启动的应用服务器名称，与绑定的第三方账户信息内容无关
   * 
   * @return
   */
  public abstract String getAppName();

  /**
   * <p> 获取基础路径索引
   * 
   * @return
   */
  protected abstract String getBaseUrlKey();

  /**
   * <p> 获取 weixin access token, <p> 不需要保证线程安全，框架中已经控制了线程安全 <p> 获取新的token对象
   * 
   * @return
   */
  protected abstract Token getTokenByRemote(String tokenKey);

  /**
   * <p> 查询令牌元
   * 
   * @param tokenKey
   * @return
   */
  public abstract TokenAtom findTokenAtom(String tokenKey);

  /**
   * <p> 保存令牌元
   * 
   * <p> 这里带有返回值，是为令牌元增加代理做扩展
   * 
   * @param tokenKey
   * @param tokenAtom
   * @return
   */
  public abstract TokenAtom saveTokenAtom(String tokenKey, TokenAtom tokenAtom);

  /**
   * <p> 获取令牌索引
   * 
   * <p> 令牌索引需要增加调用的应用关键字才是一个完整的令牌
   * 
   * @return
   */
  protected String getTokenKey() {
    return "TOKEN";
  }

  /**
   * <p> 是否自动更新token
   * 
   * <p> 如果配置为ture, 更新token到更新器会在第一个获取token后激活。
   * 
   * <p> 默认不使用中控服务
   * 
   * @return
   */
  protected boolean isAutoUpdateToken() {
    return false;
  }

  /**
   * <p> 获取令牌的提前更新时间（注意，是判定提前时间）
   * 
   * <p> 单位: 秒
   * 
   * <p> 默认值为300s, 即5分钟
   * 
   * @return
   */
  protected long getTokenAdvanceIn() {
    return 300;
  }

  /**
   * <p> 获取更新token到线程池
   * 
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
      tokenAtom.setTokenScheduler(new CustomDelayScheduler(name, s -> {
        long delay = tokenAtom.getToken().get().getExpiresIn() - getTokenAdvanceIn();
        if (delay < 0) {
          // 防止死锁，返回保留1s时间
          return 1L;
        }
        return delay;
      }, () -> this.processNewToken(tokenAtom)));
      // 启动更新服务器
      tokenAtom.getTokenScheduler().start();
    }
  }

  // ----------------------------------------------------------------ZERO Adapter
  /**
   * <p> 获取系统中常用的数据配置 返回系统中常量数据
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter(String key, Class<T> type) {
    if (type == String.class && key.equals(getTokenKey())) {
      return (T) getToken();
    }
    return super.getAdapter(key, type);
  }

  // --------------------------------------------------ZERO Config

  @Override
  public String getBaseUrl() {
    return baseUrl;
  }

  // --------------------------------------------------ZERO AccessToken

  /**
   * <p> 获取token
   */
  public String getToken(String tokenKey) {
    TokenAtom ta = getInternalCheckToken(tokenKey);
    return ta.getToken().get().getAccessToken();
  }

  /**
   * <p> 清空令牌
   */
  public void clearToken(String tokenKey, boolean stopService) {
    TokenAtom tokenAtom = getTokenAtom(tokenKey, false);
    if (tokenAtom == null) {
      return;
    }
    tokenAtom.setTokenTrigger(new Token()); // 清空access token
    if (tokenAtom.getTokenScheduler() != null && stopService) {
      tokenAtom.getTokenScheduler().stop(); // 关闭更新服务
      tokenAtom.setTokenScheduler(null); // 删除服务
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
      if (tokenAtom.getTokenKey() == null) {
        tokenAtom.setTokenKey(tokenKey);
      }
    }
    return tokenAtom;
  }

  /**
   * <p> 初始化构造AccessToken
   * 
   * <p> 初始化该内容时候，系统记性同步操作
   */
  protected synchronized TokenAtom initTokenAtom(String tokenKey) {
    TokenAtom tokenAtom = getTokenAtom(tokenKey, true);
    if (tokenAtom.getToken() != null) {
      return tokenAtom;
    }
    // token是新的，这里进行初始内容
    // 从拓扑网络中获取token
    TokenReference token = getTokenByTopology(tokenKey);
    if (token != null) {
      tokenAtom.setToken(token);
      return tokenAtom;
    }
    token = new TokenReference();
    token.set(new Token());
    token = putTokenByTopology(tokenKey, token);
    tokenAtom.setToken(token);
    return tokenAtom;
  }

  /**
   * <p> 检测token是否可用
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
   * <p> 对访问使用的令牌进行被动生命周期检测
   * 
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
      default:
        break; // access token 正常使用
    }
  }

  /**
   * <p> 对访问使用的令牌进行主动生命周期检测
   * 
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
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            break;
          case WILL_EXPIRE:
            if (tokenAtom.getTokenScheduler() == null || !tokenAtom.getTokenScheduler().isRunning()) {
              // 更新服务异常，并没有在运行， 重启服务
              restartUpdateTokenScheduler(tokenAtom.getTokenKey());
              logger.warning(tokenAtom.getTokenKey() + "令牌更新服务异常，已经通知重启。");
            }
          case VALID:
            check = false; // 检查通过，token可用使用
          default:
            break;
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
   * <p> 更新access token
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
        tokenAtom.setTokenTrigger(tkn);
        if (tkn.getNameKey() != null) {
          tokenAtom.setTokenKey(tkn.getNameKey());
        }
      }
      tokenStatistics++; // token统计增加
    } finally {
      tokenAtom.getToken().getSyncLock().set(false); // 同步表示关闭
      unlockUpdateTokenByTopology(tokenAtom.getTokenKey());
    }
  }

  /**
   * <p> 获取令牌的状态
   * 
   * @return
   */
  protected TokenStatus getAccessTokenStatus(TokenAtom tokenAtom) {
    return tokenAtom.getStatus(getTokenAdvanceIn());
  }

  // ---------------------------------------------------------------------集群同步

  /**
   * <p> 更新拓扑中的token
   * 
   * @param key
   * @param accessToken
   * @return
   */
  protected TokenReference putTokenByTopology(String tokenKey, TokenReference token) {
    return token;
  }

  /**
   * <p> 获取拓扑中的token
   * 
   * @param key
   * @return
   */
  protected TokenReference getTokenByTopology(String tokenKey) {
    return null;
  }

  /**
   * <p> 获取拓扑中的token
   * 
   * @param key
   * @return
   */
  protected List<TokenReference> getTokensByTopology() {
    return null;
  }

  /**
   * <p> 检测拓扑网络中是否有人正在修改token
   * 
   * @return
   */
  protected boolean hasUpdateTokenByTopology(String tokenKey) {
    return false;
  }

  /**
   * <p> 标记拓扑网络，当前环境正在修改token
   * 
   * <p> 当执行set操作后，应该立即解锁拓扑网络中的标记
   * 
   * @return ture 标记成功， false 标记失败，当前有其他环境正在修改token
   */
  protected boolean lockUpdateTokenByTopology(String tokenKey) {
    return true;
  }

  /**
   * <p> 解锁拓扑网络中对token更新的锁定
   * 
   * @return
   */
  protected void unlockUpdateTokenByTopology(String tokenKey) {}

  // ---------------------------------------------------------------------借助系统默认缓存处理内容
  /**
   * <p> 通过辅助的缓存内容获取token关键字
   * 
   * <p> 以下内容是对原有内容的辅助增强 <p> 也是对以前单模态下的访问令牌控制器的兼容 <p> 前期必须重写getTokenDefaultKey
   */
  protected abstract String getTokenDefaultKey();

  /**
   * <p> 获取access token
   */
  public String getToken() {
    String tokenKey = getTokenDefaultKey();
    if (tokenKey == null) {
      return null;
    }
    return getToken(tokenKey);
  }

  /**
   * <p> 清空令牌
   */
  public void clearToken(boolean stopService) {
    String tokenKey = getTokenDefaultKey();
    if (tokenKey == null) {
      return;
    }
    clearToken(tokenKey, stopService);
  }

}
