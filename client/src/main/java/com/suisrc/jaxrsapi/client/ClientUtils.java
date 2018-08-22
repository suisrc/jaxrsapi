package com.suisrc.jaxrsapi.client;

import java.io.IOException;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.resteasy.client.jaxrs.ClientBuilderFactory;

import com.suisrc.core.utils.ComUtils;
import com.suisrc.jaxrsapi.client.proxy.ProxyBuilder;
import com.suisrc.jaxrsapi.core.annotation.RemoteApi;


/**
 * 
 * @author Y13
 *
 */
public class ClientUtils {
    private static final Logger logger = Logger.getLogger(ComUtils.class.getName());
    
    /**
     * 快速远程接口访问
     * 
     * 该方法旨在为用于提供快速访问
     * 
     * 单次访问有效
     */
    public static <T, R> R getRestfulImpl(String uri, Class<T> iface, Function<T, R> getter) {
        ClientBuilder builder = ClientBuilderFactory.newBuilder();
        Client client = builder.build();
        try {
            WebTarget target = client.target(uri);
            if (iface.isAnnotationPresent(RemoteApi.class)) {
                RemoteApi path = iface.getAnnotation(RemoteApi.class);
                if (!path.value().equals("") && !path.value().equals("/")) {
                    target = target.path(path.value());
                }
            }
            T proxy = ProxyBuilder.builder(iface, target).build();
            return getter.apply(proxy);
        } finally {
            if (client != null) {
                // 释放资源
                client.close();
            }
        }
    }

    /**
     * 获取本地系统对外网的IP
     * 
     * 有时候，需要当前系统对外网IP
     * 
     * 当前方法对外网有访问权限，如果没有外网访问权限，不可以使用该方法
     * 
     * 该方法返回可能为null，如果为null表示无法获取当前IP
     */
    public static String getLocalIpByRemoteUrlRex() {
        String ipUrl = System.getProperty("TEST_LOCALHOST_IP_URL");
        if (ipUrl == null) {
            logger.info("'TEST_LOCALHOST_IP_URL' is null, can't get localhost ip.");
            return null;
        }
        HttpGet request = new HttpGet(ipUrl);
        // 防止封杀和屏蔽
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        String content = doRequest(request);
        if (content == null) {
            logger.info("HttpResponse is null, can't get localhost ip.");
            return null;
        }
        
        String ipRex = System.getProperty("TEST_LOCALHOST_IP_REX");
        if (ipRex == null) {
            return content;
        }
        Matcher matcher = Pattern.compile(ipRex).matcher(content);
        return matcher.find() ? matcher.group() : null;
    }
    
    /**
     * 远程访问
     * 
     * @param request
     * @return
     */
    public static String doRequest(HttpUriRequest request) {
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            //发送get请求
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(response.getEntity());
            }
        } catch (IOException e) {
            logger.info("远程访问[" +request.getURI().toString() + "]失败：" + e.getMessage());
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    logger.info("关闭httpclinet失败：" + e.getMessage());
                }
            }
        }
        return null;
    }

}