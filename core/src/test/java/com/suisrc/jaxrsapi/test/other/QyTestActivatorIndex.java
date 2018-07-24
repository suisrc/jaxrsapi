package com.suisrc.jaxrsapi.test.other;

import java.util.Map;

import com.suisrc.jaxrsapi.test.bean.TestServerActivatorIndex;

/**
 * 接口索引文件
 * 
 * 在生成过程中，同时生成索引文件，用于帮助在多态模式下，查找需要注入的接口信息
 * @author Y13
 *
 */
public class QyTestActivatorIndex {

    public static void main(String[] args) {
        Map<Class<?>, Class<?>> classMap = new TestServerActivatorIndex().getApiImpl();
        classMap.forEach((k, v) -> System.out.println(k + ":" + v));
    }
}
