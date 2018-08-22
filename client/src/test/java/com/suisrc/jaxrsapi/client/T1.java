package com.suisrc.jaxrsapi.client;


import org.junit.Test;

/**
 * 
 * @author Y13
 *
 */
public class T1 {

    /**
     * 
     */
    @Test
    public void test1() {
        String ipUrl = "http://pv.sohu.com/cityjson?ie=utf-8";
        String ipRex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
        System.setProperty("TEST_LOCALHOST_IP_URL", ipUrl);
        System.setProperty("TEST_LOCALHOST_IP_REX", ipRex);
        String ip = ClientUtils.getLocalIpByRemoteUrlRex();
        System.out.println(ip);
    }
}
