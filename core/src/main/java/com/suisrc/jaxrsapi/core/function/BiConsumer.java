package com.suisrc.jaxrsapi.core.function;

/**
 * 比系统的BiConsumer多了一个异常抛出
 * @author Y13
 *
 */
@FunctionalInterface
public interface BiConsumer<T, U> {
	
	/**
	 * 
	 * @param t 第一个参数
	 * @param u 第二个参数
	 * @throws Exception
	 */
	void accept(T t, U u) throws Exception;
}
