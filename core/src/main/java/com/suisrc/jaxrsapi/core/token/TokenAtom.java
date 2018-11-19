package com.suisrc.jaxrsapi.core.token;

import com.suisrc.core.listen.ListenerList;
import com.suisrc.core.reference.RefVol;
import com.suisrc.core.scheduler.Scheduler;

/**
 * <p> 一个令牌的原子类 其中包含令牌的原子对象，令牌的调度器，令牌调度器重启标识
 * 
 * <p> 该类不提供赋值方法,如果需要新建，可以通过getTokenAtom方法获取
 */
public class TokenAtom {
  public static final int TYPE_LISTENER = 0x10;
  
  /**
   * <p> token 关键字
   */
  private String tokenKey = null;

  /**
   * <p> token 强原子操作
   */
  private TokenReference token = null;

  /**
   * <p> token到调度器
   */
  private Scheduler tokenScheduler = null;

  /**
   * <p> 服务是否在重启中
   */
  private RefVol<Boolean> resetScheduler = new RefVol<>(false);
  
  /**
   * <p> 监听
   */
  private ListenerList<TokenAtom> listeners = null;

  /**
   * <p> 禁止其他地方构造方法调用
   */
  public TokenAtom() {}
  
  /**
   * <p> 是否带有监听
   * @param listener
   */
  public TokenAtom(int type) {
    if ((type & TYPE_LISTENER) != 0) {
      listeners = new ListenerList<>();
    }
  }

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

  public void setTokenKey(String tokenKey) {
    this.tokenKey = tokenKey;
  }

  public void setToken(TokenReference token) {
    this.token = token;
  }

  public void setTokenTrigger(Token token) {
    this.token.set(token);
    if (listeners != null) {
      // 触发监听
      listeners.trigger("NEW_TOKEN", this);
    }
  }

  public void setTokenScheduler(Scheduler tokenScheduler) {
    this.tokenScheduler = tokenScheduler;
  }

  /**
   * <p> 判断token的状态
   * 
   * @param advanceIn
   * @return
   */
  public TokenStatus getStatus(Long advanceIn) {
    return token.get().getStatus(advanceIn);
  }
  
  /**
   * <p> 获取监听
   * @return
   */
  public ListenerList<TokenAtom> getListeners() {
    return listeners;
  }
}
