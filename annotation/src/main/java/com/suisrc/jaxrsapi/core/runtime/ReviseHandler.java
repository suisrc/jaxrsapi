package com.suisrc.jaxrsapi.core.runtime;

/**
 * 拦截数据，修正数据的接口
 * @author Y13
 *
 */
@FunctionalInterface
public interface ReviseHandler<T> {
	/** 
	 * 方法名称 
	 */
	final String METHOD = "accept";

	/**
	 * 修正数据，并把修正后的结果返回
	 * @param value
	 * @return
	 */
	public T accept( T value );
}
