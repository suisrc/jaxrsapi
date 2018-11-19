package com.suisrc.jaxrsapi.core.token;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 
 * 为什么需要壳，可是为了配置远程访问增加更好的实现 同时在线程控制上做的最好的兼容
 * 
 * 为什么在Access Token上加两层壳呢？？见仁见智吧
 * 
 * @author Y13
 *
 */
public class TokenReference implements Serializable {
  private static final long serialVersionUID = 5200394222671556768L;

  /**
   * token实体
   */
  private AtomicReference<Token> value;

  /**
   * 是否在同步 access token 正在同步中，该字段是给异步同时时候使用的，避免多次异步同步更新
   */
  private AtomicBoolean syncLock = new AtomicBoolean(false);

  public TokenReference() {
    initConstruct();
  }

  /**
   * 初始化
   */
  protected void initConstruct() {
    value = new AtomicReference<>();
  }

  /**
   * 获取
   * 
   * @return
   */
  public Token get() {
    return value.get();
  }

  /**
   * 更新
   * 
   * @param value
   */
  public TokenReference set(Token newValue) {
    value.set(newValue);
    // Intel 64/IA-32下写操作之间不会发生重排序，即在处理器中，构建SomeThing对象与赋值到object这两个操作之间的顺序性是可以保证的。
    // 这样看起来，仅仅使用volatile来避免重排序是多此一举的。但是，Java编译器却可能生成重排序后的指令。但令人高兴的是，Oracle的JDK
    // 中提供了Unsafe. putOrderedObject，Unsafe. putOrderedInt，Unsafe. putOrderedLong这三个方法，JDK会在执行这三个方法时插
    // 入StoreStore内存屏障，避免发生写操作重排序。而在Intel 64/IA-32架构下，StoreStore屏障并不需要，Java编译器会将StoreStore屏
    // 障去除。比起写入volatile变量之后执行StoreLoad屏障的巨大开销，采用这种方法除了避免重排序而带来的性能损失以外，不会带来其它的性
    // 能开销。 (好吧，看不懂就算了),如果优化实现下面的方法
    // value.lazySet(newValue);

    return this;
  }

  /**
   * 获取同步标记锁状态
   * 
   * @return
   */
  public AtomicBoolean getSyncLock() {
    return syncLock;
  }
  
}
