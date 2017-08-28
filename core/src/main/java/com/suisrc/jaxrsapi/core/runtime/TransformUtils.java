package com.suisrc.jaxrsapi.core.runtime;

import com.suisrc.common.iface.ITransformString;
import com.suisrc.common.transform.StringTransform;

/**
 * 类型转换
 * @author Y13
 */
public class TransformUtils {

	/**
	 * 转换的方法
	 */
	static final String METHOD = "transform";

	/**
	 * 数据转换
	 * @param type   返回的类型
	 * @param value  数值
	 * @return
	 */
	public static <T> T transform(Class<T> type, String value) {
		if( value.isEmpty() || value.toLowerCase().equals("null") ) {
			return null;
		}
		ITransformString<T> ts = StringTransform.createTransform(type);
		return ts.fromString(value);
	}
}
