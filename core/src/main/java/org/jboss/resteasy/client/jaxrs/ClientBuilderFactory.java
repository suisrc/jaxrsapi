package org.jboss.resteasy.client.jaxrs;

import javax.ws.rs.client.ClientBuilder;

/**
 * Client Builder Factory工程类
 * 
 * 由于在实际生成环境中发现，Resteasy使用的HttpClient存在很多差异，在4.3.6版本是一个分水岭
 * 所以在这里提供程序的因外部包不同而导致的兼容性问题
 * 
 * @see org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
 * @author Y13
 *
 */
public class ClientBuilderFactory {

	private static final boolean useOldHTTPClient = Boolean.getBoolean("org.jboss.resteasy.client.useOldHTTPClient");
	private static final boolean newHTTPClientAvailable;
	static {
		boolean res = true;
		try {
			Class.forName(HttpClientBuilder43.class.getName());
		} catch (Throwable t) {
			res = false;
		}
		newHTTPClientAvailable = res;
	}

	public static ClientBuilder newBuilder() {
		return new ResteasyClientBuilder();
//		return ClientBuilder.newBuilder();
	}

	@SuppressWarnings("deprecation")
	public static void initHttpEngineThreadSaft(ResteasyClientBuilder that) {
		ClientHttpEngine engine = null;
		if (useOldHTTPClient || !newHTTPClientAvailable) {
			engine = ClientHttpEngineBuilder4.initDefaultEngine4(that);
		} else {
			engine = ClientHttpEngineBuilder43.initDefaultEngine43(that);
		}
		that.httpEngine(engine);
	}

}
