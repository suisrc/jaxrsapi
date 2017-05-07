package com.suisrc.jaxrsapi.core.func;

/**
 * 如何修正数据
 * @author Y13
 *
 */
@FunctionalInterface
public interface InterceptHandler<T> {
	/** 方法名称 */
	final String METHOD = "accept";

	/**
	 * 具体如何修正数据
	 * @param value
	 * @return
	 */
	public T accept( T value );
}
