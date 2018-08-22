package org.jboss.resteasy.client.jaxrs;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.params.HttpParams;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;

import com.suisrc.core.utils.ReflectionUtils;

/**
 * http client builder 为旧版本的client(4.3版本以下)提供技术支持
 * 
 * @author Y13
 *
 */
@SuppressWarnings("deprecation")
public class ClientHttpEngineBuilder4 {

    public static ClientHttpEngine initDefaultEngine4(ResteasyClientBuilder that) {
        try {
            ApacheHttpClient4Engine engine = (ApacheHttpClient4Engine) ReflectionUtils.invokeDecared(ResteasyClientBuilder.class,
                    that, "initDefaultEngine", null, null);
            // 基本属性
            HostnameVerifier verifier = engine.getHostnameVerifier();
            HttpParams httpParams = engine.getHttpClient().getParams();
            SchemeRegistry registry = engine.getHttpClient().getConnectionManager().getSchemeRegistry();
            SSLContext sslContext = engine.getSslContext();
            // 基本属性
            Object connectionPoolSize = ReflectionUtils.invokeGetField(ResteasyClientBuilder.class, that, "connectionPoolSize");
            Object connectionTTL = ReflectionUtils.invokeGetField(ResteasyClientBuilder.class, that, "connectionTTL");
            Object connectionTTLUnit = ReflectionUtils.invokeGetField(ResteasyClientBuilder.class, that, "connectionTTLUnit");
            Object maxPooledPerRoute = ReflectionUtils.invokeGetField(ResteasyClientBuilder.class, that, "maxPooledPerRoute");
            Object defaultProxy = ReflectionUtils.invokeGetField(ResteasyClientBuilder.class, that, "defaultProxy");
            Object responseBufferSize = ReflectionUtils.invokeGetField(ResteasyClientBuilder.class, that, "responseBufferSize");

            ClientHttpEngineProxy4 proxy = new ClientHttpEngineProxy4();
            proxy.setRegistrySafe(registry);
            proxy.setConnectionPoolSizeSafe(connectionPoolSize == null ? 0 : (int) connectionPoolSize);
            proxy.setConnectionTTLSafe(connectionTTL == null ? 0 : (long) connectionTTL);
            proxy.setConnectionTTLUnitSafe((TimeUnit) connectionTTLUnit);
            proxy.setMaxPooledPerRouteSafe(maxPooledPerRoute == null ? 0 : (int) maxPooledPerRoute);
            proxy.setRequestConfigBuilderSafe(httpParams);
            // proxy.setConnManagerSafe(cm);
            proxy.setProxySafe((HttpHost) defaultProxy);
            proxy.setResponseBufferSizeSafe(responseBufferSize == null ? 0 : (int) responseBufferSize);
            proxy.setHostnameVerifierSafe(verifier);
            // theContext may be null. We can't really support this with Apache Client.
            proxy.setSslContextSafe(sslContext);
            // 注销
            engine.finalize();

            return proxy;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
