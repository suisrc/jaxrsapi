package com.suisrc.jaxrsapi.core.runtime;

/**
 * 重新请求断言， 判断就是否需要进行重试
 * 
 * 注意，断言重试次数，受Retry注解限制
 * 
 * @author Y13
 *
 */
public interface RetryPredicate<T> {
  /**
   * 方法名称
   */
  final String METHOD = "test";

  /**
   * 断言是否需要记性重新访问
   * 
   * @param count 可以重试的总次数
   * @param time 剩余次数（特别注意，这里是剩余次数，当剩余次数为0，即使该方法不返回false,请求也会强制结束）
   * @param result 请求的结果
   * @param e 请求发生有异常，异常的内容
   * @return true, 需要重新发送请求，进行重试，false,不进行重试
   */
  boolean test(int count, int time, T result, Exception e);

}
