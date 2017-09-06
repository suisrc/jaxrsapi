package com.suisrc.jaxrsapi.test;

import com.suisrc.jaxrsapi.core.factory.NSCF;
import com.suisrc.jaxrsapi.test.bean.TestServerActivator;

/**
 * 测试入口
 * @author ICG-DL-Y13
 *
 */
public class TMain {
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
//        NSCF.build("D:/test/target/", TestServerActivator.class);
        NSCF.build("D:/test/target/", TestServerActivator.class);
        System.out.println("OK");
    }

}
