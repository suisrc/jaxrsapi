package com.suisrc.jaxrsapi.core;

/**
 * 静态变量
 * @author Y13
 *
 */
public interface Consts {
	
	//-------------------------value prefix------------------------------//
	
	/**
	 * 注入属性二级变名
	 */

	/**
	 * 系统环境变量
	 */
	final String PRE_ENVIRONMENT = "E:";
	
	/**
	 * 系统属性变量
	 */
	final String PRE_SYSTEM = "S:";
	
	/**
	 * 全局环境变量
	 */
	final String PRE_GLOBAL = "G:";
	
	/**
	 * 线程环境变量
	 */
	final String PRE_THREAD = "T:";

	//----------------------------proxy----------------------------------//
	/**
	 * 客户端代理的名字
	 */
	final String FIELD_PROXY = "proxy";
	/**
	 * 客户端激活器的名字
	 */
	final String FIELD_ACTIVATOR = "activator";
	/**
	 * 客户端的本身自己的名字
	 */
	final String FIELD_THIS = "this";
	
	/**
	 * 注入的时候@Named的间隔符
	 */
	String separator = "/";
	
	//------------------------------------------KEY--------------------------------//
	/**
	 * 远程API在系统中运行的模式，如果是单远程模式，只需要使用简单的@Inject即可，
	 * 如果同时访问多个远程服务器，需要在使用注入的使用通过@Named进行分离
	 * 这里所谓的多个，是同一个restful接口对应多个服务器的情况
	 * 如果不愿意使用@Named,可以使用拷贝多份restful接口解决这个问题。这个使用
	 * 系统应该运行与单模式
	 * 
	 * 多模式：同一个restful接口对应多台远程服务器
	 */
	String KEY_REMOTE_API_NULTI_MODE = "com.suisrc.remote-api.runtime.multi-mode";
	
	/**
	 * 默认的变量容器
	 * 该配置内容只能是 E: S: G: T: 其中情况，其他无效
	 */
	String KEY_VALUE_PREFIX_DEFAULT = "com.suisrc.remote-api.values.default-container";

	
	/**
	 * 获取API的基本路径地址
	 */
	String BASE_URL = "BASE_URL";

	/**
	 * 请求内容key,用于线程缓存中获取请求内容
	 */
	String CONTAINER_REQUEST_CONTEXT = "one-container-request-context";
}
